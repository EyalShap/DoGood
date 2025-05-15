package com.dogood.dogoodbackend.domain.users;

import com.dogood.dogoodbackend.domain.externalAIAPI.CVSkillsAndPreferencesExtractor;
import com.dogood.dogoodbackend.domain.externalAIAPI.SkillsAndPreferences;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.emailverification.VerificationData;
import com.dogood.dogoodbackend.api.userrequests.RegisterRequest; // Import RegisterRequest DTO

import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
public class UsersFacade {
    private VolunteeringFacade volunteeringFacade;
    private UserRepository repository;
    private AuthFacade authFacade;
    private ReportsFacade reportsFacade;
    private NotificationSystem notificationSystem;
    private CVSkillsAndPreferencesExtractor extractor;

    private final Random random = new SecureRandom();
    private final EmailSender emailSender; // This IS final and needs to be initialized
    private final VerificationCacheService verificationCache; // Ensure type is correct

    public UsersFacade(UserRepository repository, AuthFacade authFacade, CVSkillsAndPreferencesExtractor extractor) {
        this.repository = repository;
        this.authFacade = authFacade;
        this.extractor = extractor;
        this.emailSender = null;
        this.verificationCache = null;
    }
    public UsersFacade(UserRepository repository, AuthFacade authFacade, CVSkillsAndPreferencesExtractor extractor, EmailSender emailSender, VerificationCacheService verificationCache) {
        this.repository = repository;
        this.authFacade = authFacade;
        this.extractor = extractor;
        this.emailSender = emailSender; // NOW CORRECTLY ASSIGNED
        this.verificationCache = verificationCache; // NOW CORRECTLY ASSIGNED
    }

    public void setVolunteeringFacade(VolunteeringFacade volunteeringFacade) {
        this.volunteeringFacade = volunteeringFacade;
    }

    public void setReportsFacade(ReportsFacade reportsFacade) {
        this.reportsFacade = reportsFacade;
    }

    public void setNotificationSystem(NotificationSystem notificationSystem) {
        this.notificationSystem = notificationSystem;
    }
    // VERIFICATION START
    private String generateVerificationCode() {
        return String.format("%06d", this.random.nextInt(1000000));
    }
    // VERIFICATION END

    public String login(String username, String password) {
        User user = getUser(username);
        // VERIFICATION START
        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Email not verified. Please verify your email before logging in.");
        }
        // VERIFICATION END

        for(String email : user.getEmails()) {
            if(reportsFacade.isBannedEmail(email)) {
                throw new IllegalArgumentException(username + " is banned from the system.");
            }
        }

        boolean correctPassword = user.checkPassword(password);
        if (!correctPassword) {
            throw new IllegalArgumentException("Invalid password given for user " + username);
        }
        repository.saveUser(user);
        String accessToken = authFacade.generateToken(username);
        return accessToken;
    }

    public void logout(String token) {
        String username = authFacade.getNameFromToken(token); // Throws an exception if the given access token doesn't exist (user isn't logged in).
        authFacade.invalidateToken(token); // Invalidates the token if it isn't invalidated (isn't logged out already), otherwise (somehow) throws an exception.
    }

    public void register(String username, String password, String name, String email, String phone, Date birthDate, String profilePicUrl) {
        User newUser = null;
        try {
            User user = getUser(username);
            // if user with the same username exists, cannot register it again
            throw new IllegalArgumentException("Register failed - username " + username + " already exists.");
        } catch (IllegalArgumentException e) {

            if(reportsFacade.isBannedEmail(email)) {
                throw new IllegalArgumentException(String.format("The email %s is banned from the system.", email));
            }

            newUser = repository.createUser(username, email, name, password, phone, birthDate,profilePicUrl);
        }
        // VERIFICATION START
        if (newUser != null) { // User was successfully created in the catch block
            if (this.emailSender == null || this.verificationCache == null) {
                // This indicates a configuration problem (wrong constructor used by FacadeManager)
                System.err.println("CRITICAL: EmailService or VerificationCacheService not initialized in UsersFacade. Cannot send verification email.");
                // Depending on policy, you might throw an exception here or log critically.
                // For now, we'll let it proceed without verification if services are null, but this is not ideal.
                // throw new IllegalStateException("EmailService or VerificationCacheService not configured.");
            } else {
                String verificationCode = generateVerificationCode();
                RegisterRequest cachedUserData = new RegisterRequest();
                cachedUserData.setUsername(username);
                // As per prompt: "The password in userData within RegisterRequest should already be hashed."
                // The 'password' variable here is the raw password.
                cachedUserData.setPassword(Cryptography.hashPassword(password));
                cachedUserData.setName(name);
                cachedUserData.setEmail(email);
                cachedUserData.setPhone(phone);
                cachedUserData.setBirthDate(birthDate);
                cachedUserData.setProfilePicUrl(profilePicUrl);

                verificationCache.storeVerificationData(email.toLowerCase(), cachedUserData, verificationCode);
                emailSender.sendVerificationCodeEmail(email, username, verificationCode);
            }
        }
        // VERIFICATION END
    }

    //this is used only for the setup so we could ignore the "formal setup" and just give it the emailVerified toekn becuase it is only for testing
    public void register(String username, String password, String name, String email, String phone, Date birthDate) {
        User newUser = null;
        try {
            User user = getUser(username);
            // if user with the same username exists, cannot register it again
            throw new IllegalArgumentException("Register failed - username " + username + " already exists.");
        } catch (IllegalArgumentException e) {

            if(reportsFacade.isBannedEmail(email)) {
                throw new IllegalArgumentException(String.format("The email %s is banned from the system.", email));
            }

            newUser = repository.createUser(username, email, name, password, phone, birthDate,"");
            newUser.setEmailVerified(true);
            repository.saveUser(newUser);
        }

    }
    // VERIFICATION START
    public String verifyEmail(String username, String code) {
        if (this.verificationCache == null || this.repository == null) {
            throw new IllegalStateException("VerificationCacheService or UserRepository not configured in UsersFacade.");
        }
        Optional<User> userOptional = repository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOptional.get();

        if (user.isEmailVerified()) {
            return "Email already verified.";
        }

        String emailToVerify = user.getEmails().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("User has no email address for verification."));

        Optional<VerificationData> verificationDataOptional = verificationCache.getAndValidateVerificationData(emailToVerify.toLowerCase(), code);

        if (verificationDataOptional.isPresent()) {
            user.setEmailVerified(true);
            repository.saveUser(user);
            verificationCache.removeVerificationData(emailToVerify.toLowerCase());
            return "Email verified successfully.";
        } else {
            // Check if an entry exists (even if expired or code is wrong) to give a more specific message.
            // getVerificationData only returns non-expired data.
            Optional<VerificationData> currentDataIfAny = verificationCache.getVerificationData(emailToVerify.toLowerCase());
            if(currentDataIfAny.isEmpty()){
                // This means:
                // 1. No code was ever sent for this email.
                // 2. Code was already used and removed.
                // 3. Code expired and was cleaned up by getVerificationData or getAndValidateVerificationData.
                return "Invalid or expired verification code. It might have expired, been used, or was never sent.";
            } else {
                // Data exists in cache and is not expired, so the submitted code must be wrong.
                return "Invalid verification code.";
            }
        }
    }
    // VERIFICATION END
    // FORGOT_PASSWORD START
// Helper method already exists for generating verification codes, will reuse:
// private String generateVerificationCode() {
//     return String.format("%06d", this.random.nextInt(1000000));
// }

    public void forgotPassword(String email) {
        if (emailSender == null || verificationCache == null || repository == null) {
            // Log critical error, but don't throw to the client to prevent enumeration
            System.err.println("CRITICAL: Required services (Email, Cache, Repository) not configured in UsersFacade for forgotPassword.");
            return; // Silently return
        }

        Optional<User> userOptional = repository.findByEmail(email.toLowerCase());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String username = user.getUsername(); // Get username for the email
            String code = generateVerificationCode();

            // To use the existing VerificationCacheService.storeVerificationData,
            // which expects a RegisterRequest, we create a minimal one.
            // This RegisterRequest is just a carrier for context if needed by the cache,
            // or to satisfy the method signature. It won't be used for actual registration.
            RegisterRequest dummyUserData = new RegisterRequest();
            dummyUserData.setUsername(username); // Store username for potential context
            dummyUserData.setEmail(email);       // Store email for potential context

            // Assuming VerificationCacheService.storeVerificationData is:
            // storeVerificationData(String emailKey, RegisterRequest userData, String code)
            // The actual record VerificationData(String code, Instant expiry, RegisterRequest userData)
            // will be created inside verificationCacheService.
            verificationCache.storeVerificationData(email.toLowerCase(), dummyUserData, code);
            emailSender.sendVerificationCodeEmail(email, username, code); // Send email
        }
        // Always return without error to prevent email enumeration
    }

    public boolean verifyPasswordResetCode(String username, String code) {
        if (verificationCache == null || repository == null) {
            System.err.println("CRITICAL: Required services (Cache, Repository) not configured in UsersFacade for verifyPasswordResetCode.");
            // Consider throwing an exception here as this is an internal state problem
            // or return false and let the service layer handle the response.
            throw new IllegalStateException("Verification services not properly configured.");
        }

        Optional<User> userOptional = repository.findByUsername(username);
        if (userOptional.isEmpty()) {
            // User not found, code cannot be valid for them.
            return false;
        }
        User user = userOptional.get();
        // Assuming user has at least one email and the first one is primary for this context
        String email = user.getEmails().stream().findFirst().orElse(null);

        if (email == null) {
            // User has no email, should not happen for a registered user needing password reset.
            return false;
        }

        Optional<VerificationData> verificationDataOptional = verificationCache.getAndValidateVerificationData(email.toLowerCase(), code);

        // Additionally, we might want to check if the userData stored (our dummy RegisterRequest)
        // matches the username, if we stored it for this purpose.
        // For now, just validating code against email.
        return verificationDataOptional.isPresent();
    }

    public String resetPassword(String username, String newPassword, String code) {
        if (verificationCache == null || repository == null) {
            throw new IllegalStateException("Verification or persistence services not properly configured.");
        }

        Optional<User> userOptional = repository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return "User not found.";
        }
        User user = userOptional.get();
        String email = user.getEmails().stream().findFirst().orElse(null);

        if (email == null) {
            return "User email not found.";
        }

        Optional<VerificationData> verificationDataOptional = verificationCache.getAndValidateVerificationData(email.toLowerCase(), code);

        if (verificationDataOptional.isPresent()) {
            // Code is valid, proceed to reset password
            user.setPasswordHash(Cryptography.hashString(newPassword)); // Assuming User model has setPasswordHash or similar
            repository.saveUser(user);
            verificationCache.removeVerificationData(email.toLowerCase()); // Important: consume the code
            return "Password reset successfully.";
        } else {
            return "Invalid or expired verification code.";
        }
    }
// FORGOT_PASSWORD END

    public void updateProfilePicture(String username, String profilePicUrl) {
        User user = getUser(username);
        user.setProfilePicUrl(profilePicUrl);
        repository.saveUser(user);
    }

    public boolean isAdmin(String username) {
        User user = getUser(username);
        return user.isAdmin();
    }

    public void registerAdmin(String username, String password, String name, String email, String phone, Date birthDate) {
        try {
            User user = getUser(username);
            // if user with the same username exists, cannot register it again
            throw new IllegalArgumentException("Register failed - username " + username + " already exists.");
        } catch (IllegalArgumentException e) {

            if(reportsFacade.isBannedEmail(email)) {
                throw new IllegalArgumentException(String.format("The email %s is banned from the system.", email));
            }

            User newUser = repository.createUser(username, email, name, password, phone, birthDate,"");
            newUser.setAdmin(true);
            newUser.setEmailVerified(true);
            repository.saveUser(newUser);
        }
    }


    public User getUser(String username) {
        return repository.getUser(username);
    }

    public String getUserIdByToken(String token) {
        return authFacade.getNameFromToken(token);
    }

    public User getUserByToken(String token) {
        return getUser(authFacade.getNameFromToken(token));
    }

    public void registerFcmToken(String username, String fcmToken){
        User user = getUser(username);
        user.addToken(fcmToken);
        repository.saveUser(user);
    }

    public Set<String> getFcmTokens(String username){
        User user = getUser(username);
        return user.getFcmTokens();
    }

    public void expireFcmTokens(String username, Set<String> expiredTokens){
        User user = getUser(username);
        for(String fcmToken : expiredTokens){
            user.expireToken(fcmToken);
        }
        repository.saveUser(user);
    }

    public boolean userExists(String username) {
        try {
            User user = getUser(username);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void updateUserFields(String username, String password, List<String> emails, String name, String phone){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }

        for(String email : emails) {
            if(reportsFacade.isBannedEmail(email)) {
                throw new IllegalArgumentException(email + " is banned from the system.");
            }
        }

        User user = getUser(username);
        if(password == null){
            repository.updateUserFields(
                    username == null ? user.getUsername() : username,
                    emails == null ? user.getEmails() : emails,
                    name == null ? user.getName() : name,
                    phone == null ? user.getPhone() : phone);
        }
        else{
            repository.updateUserFields(
                    username == null ? user.getUsername() : username,
                    emails == null ? user.getEmails() : emails,
                    name == null ? user.getName() : name,
                    password,
                    phone == null ? user.getPhone() : phone);
        }
    }

    public void updateUserSkills(String username, List<String> skills){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.updateSkills(skills);
        repository.saveUser(user);
    }

    public void updateUserPreferences(String username, List<String> categories){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.updatePreferences(categories);
        repository.saveUser(user);
    }

    public void addUserVolunteering(String username, int volunteeringId){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.addVolunteering(volunteeringId);
        repository.saveUser(user);
    }

    public void addUserOrganization(String username, int organizationId){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.addOrganization(organizationId);
        repository.saveUser(user);
    }

    public void removeUserVolunteering(String username, int volunteeringId){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.removeVolunteering(volunteeringId);
        repository.saveUser(user);
    }

    public void addUserVolunteeringHistory(String username, VolunteeringDTO volunteeringDTO){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.addVolunteeringToHistory(volunteeringDTO);
        repository.saveUser(user);
    }

    public List<String> getUserSkills(String username){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        return user.getSkills();
    }

    public List<String> getUserPreferences(String username){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        return user.getPreferredCategories();
    }

    public List<VolunteeringDTO> getUserVolunteeringHistory(String username){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        return user.getVolunteeringsInHistory();
    }

    public void removeUserOrganization(String actor, int organizationId) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(actor);
        user.removeOrganization(organizationId);
        repository.saveUser(user);
    }

    public List<HourApprovalRequest> getApprovedHours(String username) {
        User user = getUser(username);
        List<Integer> allIds = new LinkedList<>();
        allIds.addAll(user.getVolunteeringIds());
        allIds.addAll(user.getVolunteeringsInHistory().stream().map(dto -> dto.getId()).toList());
        return volunteeringFacade.getUserApprovedHours(user.getUsername(), allIds);
    }

    public Map<String, Double> leaderboard() {
        List<User> allUsers = repository.getAllUsers();
        Map<String, Double> leaderboardMap = new HashMap<>();

        for(User user : allUsers) {
            if(user.getLeaderboard()) {
                List<HourApprovalRequest> hours = getApprovedHours(user.getUsername());
                double totalUserHours = hours.stream().mapToDouble(HourApprovalRequest::getTotalHours).sum();
                leaderboardMap.put(user.getUsername(), totalUserHours);
            }
        }
        Map<String, Double> sortedLeaderboard = leaderboardMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue()) // Ascending order
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // Merge function (not needed here)
                        LinkedHashMap::new // Preserve order
                ));
        return sortedLeaderboard;
    }

    public void setLeaderboard(String username, boolean leaderboard) {
        User user = repository.getUser(username);
        user.setLeaderboard(leaderboard);
        repository.saveUser(user);
    }

    public boolean isBanned(String username) {
        List<String> userEmails = getUser(username).getEmails();
        for(String email : userEmails) {
            if(reportsFacade.isBannedEmail(email)) {
                return true;
            }
        }
        return false;
    }

    public void banUser(String username, String actor) {
        List<String> userEmails = getUser(username).getEmails();
        for(String email : userEmails) {
            reportsFacade.banEmail(email, actor);
        }
        reportsFacade.removeUserReports(username);
    }

    public List<String> getAllUserEmails() {
        List<User> allUsers = repository.getAllUsers();
        Set<String> allEmails = new HashSet<>();

        for(User user : allUsers) {
            allEmails.addAll(user.getEmails());
        }
        return new ArrayList<>(allEmails);
    }

    public void uploadCV(String username, MultipartFile cvPdf) {
        repository.uploadCV(username, cvPdf);
    }

    public byte[] getCV(String username) {
        return repository.getCV(username);
    }

    public void generateSkillsAndPreferences(String username) {
        User user = repository.getUser(username);
        SkillsAndPreferences skillsAndPreferences = extractor.getSkillsAndPreferences(user.getCv(), new HashSet<>(user.getSkills()), new HashSet<>(user.getPreferredCategories()));

        Set<String> aiSkills = new HashSet<>(skillsAndPreferences.getSkills());
        Set<String> aiPreferences = new HashSet<>(skillsAndPreferences.getPreferences());

        aiSkills.addAll(user.getSkills());
        aiPreferences.addAll(user.getPreferredCategories());

        updateUserSkills(username, new ArrayList<>(aiSkills));
        updateUserPreferences(username, new ArrayList<>(aiPreferences));
    }

    public List<String> getAllUsername() {
        List<User> allUsers = repository.getAllUsers();
        List<String> usernames = allUsers.stream().map(user -> user.getUsername()).collect(Collectors.toList());
        return usernames;
    }
}