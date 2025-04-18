package com.dogood.dogoodbackend.domain.users;

import com.dogood.dogoodbackend.domain.externalAIAPI.*;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public UsersFacade(UserRepository repository, AuthFacade authFacade, CVSkillsAndPreferencesExtractor extractor) {
        this.repository = repository;
        this.authFacade = authFacade;
        this.extractor = extractor;
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

    public String login(String username, String password) {
        User user = getUser(username);

        for(String email : user.getEmails()) {
            if(reportsFacade.isBannedEmail(email)) {
                throw new IllegalArgumentException(username + " is banned from the system.");
            }
        }

        boolean correctPassword = user.checkPassword(password);
        if (!correctPassword) {
            throw new IllegalArgumentException("Invalid password given for user " + username);
        }
        String accessToken = authFacade.generateToken(username);
        return accessToken;
    }

    public void logout(String token) {
        String username = authFacade.getNameFromToken(token); // Throws an exception if the given access token doesn't exist (user isn't logged in).
        authFacade.invalidateToken(token); // Invalidates the token if it isn't invalidated (isn't logged out already), otherwise (somehow) throws an exception.
    }

    public void register(String username, String password, String name, String email, String phone, Date birthDate, String profilePicUrl) {
        try {
            User user = getUser(username);
            // if user with the same username exists, cannot register it again
            throw new IllegalArgumentException("Register failed - username " + username + " already exists.");
        } catch (IllegalArgumentException e) {

            if(reportsFacade.isBannedEmail(email)) {
                throw new IllegalArgumentException(String.format("The email %s is banned from the system.", email));
            }

            repository.createUser(username, email, name, password, phone, birthDate,profilePicUrl);
        }
    }

    public void register(String username, String password, String name, String email, String phone, Date birthDate) {
        try {
            User user = getUser(username);
            // if user with the same username exists, cannot register it again
            throw new IllegalArgumentException("Register failed - username " + username + " already exists.");
        } catch (IllegalArgumentException e) {

            if(reportsFacade.isBannedEmail(email)) {
                throw new IllegalArgumentException(String.format("The email %s is banned from the system.", email));
            }

            repository.createUser(username, email, name, password, phone, birthDate,"");
        }
    }

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

            repository.createUser(username, email, name, password, phone, birthDate,"");
            repository.setAdmin(username, true);
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