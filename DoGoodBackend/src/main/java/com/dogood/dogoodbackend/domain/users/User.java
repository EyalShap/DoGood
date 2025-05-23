package com.dogood.dogoodbackend.domain.users;

import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.*;

@Entity
public class User {
    @Id
    @Column(columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String username;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> emails;
    private String name;
    @JsonIgnore
    private String passwordHash;
    private String profilePicUrl;
    private String phone;
    private Date birthDate;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> preferredCategories;
//    private notification preferences?
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> volunteeringIds;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<VolunteeringInHistory> volunteeringsInHistory;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> myOrganizationIds;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> skills;
    private boolean isStudent;
    private boolean isAdmin;
    private boolean leaderboard;

    private boolean remindActivity;
    private boolean notifyRecommendations;

    @ElementCollection(fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<String> fcmTokens;

    @Lob
    @Column(name = "cv", columnDefinition = "LONGBLOB")
    private byte[] cv;

    // --- New Fields ---
    private boolean emailVerified = false;

    public User() {
    }

    public User(String username, String email, String name, String password, String phone, Date birthDate, String profilePicUrl) {
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Given username isn't valid, it has to be alphanumeric, contain at least 1 letter and be at least 4 characters long.");
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException(("Given password isn't valid, it has to be at least 6 characters long."));
        }
        this.username = username;
        this.emails = new ArrayList<>();
        this.emails.add(email);
        this.name = name;
        this.passwordHash = Cryptography.hashString(password);
        this.phone = phone;
        this.birthDate = birthDate;
        this.preferredCategories = new ArrayList<>();
        this.volunteeringIds = new ArrayList<>();
        this.volunteeringsInHistory = new ArrayList<>();
        this.myOrganizationIds = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.isStudent = checkStudentEmail(email);
        this.isAdmin = false;
        this.leaderboard = true;
        this.profilePicUrl = profilePicUrl;
        // Initialize new fields to defaults
        this.emailVerified = false;
        this.remindActivity = false;
        this.notifyRecommendations = true;
    }

    public User(String username, String email, String name, String password, String phone, Date birthDate) {
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Given username isn't valid, it has to be alphanumeric, contain at least 1 letter and be at least 4 characters long.");
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException(("Given password isn't valid, it has to be at least 6 characters long."));
        }
        this.username = username;
        this.emails = new ArrayList<>();
        this.emails.add(email);
        this.name = name;
        this.passwordHash = Cryptography.hashString(password);
        this.phone = phone;
        this.birthDate = birthDate;
        this.preferredCategories = new ArrayList<>();
        this.volunteeringIds = new ArrayList<>();
        this.volunteeringsInHistory = new ArrayList<>();
        this.myOrganizationIds = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.isStudent = checkStudentEmail(email);
        this.isAdmin = false;
        this.leaderboard = true;
        this.profilePicUrl = "";
        // Initialize new fields to defaults
        this.emailVerified = false;
        this.remindActivity = false;
        this.notifyRecommendations = true;
    }

    public User(String username, List<String> emails, String name, String password, String phone, Date birthDate, List<String> preferredCategories, List<Integer> volunteeringIds, List<VolunteeringDTO> volunteeringsInHistory, List<Integer> myOrganizationIds, List<String> skills, boolean isStudent, boolean isAdmin) {
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Given username isn't valid, it has to be alphanumeric, contain at least 1 letter and be at least 4 characters long.");
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException(("Given password isn't valid, it has to be at least 6 characters long."));
        }
        this.username = username;
        this.emails = new ArrayList<>(emails);
        this.name = name;
        this.passwordHash = Cryptography.hashString(password);
        this.phone = phone;
        this.birthDate = birthDate;
        this.preferredCategories = preferredCategories;
        this.volunteeringIds = volunteeringIds;
        this.volunteeringsInHistory = volunteeringsInHistory.stream().map(dto -> new VolunteeringInHistory(dto)).toList();
        this.myOrganizationIds = myOrganizationIds;
        this.skills = skills;
        this.isStudent = isStudent;
        this.isAdmin = isAdmin;
        this.profilePicUrl = "";
        // Initialize new fields to defaults
        this.emailVerified = false;
        this.remindActivity = false;
        this.notifyRecommendations = true;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    // --- Other necessary methods ---
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }


    private boolean isValidUsername(String username) {
        return username != null && username.length() >= 4 && username.chars().allMatch(Character::isLetterOrDigit) && username.chars().anyMatch(Character::isLetter);
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // NOTE: when the user still has the old password hash, we update it to the new bcrypt hash, to support backward compatibility and automatic migration to new secured algorithm
    public boolean checkPassword(String password) {
        if(!passwordHash.contains("$")) { // bcrypt check, to support backward compatibility
           if(!passwordHash.equals(Cryptography.hashStringOld(password))){
               return false;
           }
           passwordHash = Cryptography.hashPassword(password); // update current hash to the new form
           return true;
        }
        return Cryptography.checkPassword(password,passwordHash);
    }

    private static boolean checkStudentEmail(String email) {
        return email.endsWith("ac.il"); // TODO: make this better
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }

    public void updateFields(List<String> emails, String name, String phoneNumber) {
        this.emails = emails;
        this.name = name;
        this.phone = phoneNumber;
    }

    public void updateFields(List<String> emails, String name, String password, String phoneNumber) {
        this.emails = emails;
        this.name = name;
        this.passwordHash = Cryptography.hashString(password);
        this.phone = phoneNumber;
    }

    public void updatePassword(String newPassword) {
        this.passwordHash = Cryptography.hashString(newPassword);
    }

    public void updateSkills(List<String> skills) {
        this.skills = skills;
    }

    public void updatePreferences(List<String> preferredCategories){
        this.preferredCategories = preferredCategories;
    }

    public void addVolunteering(int volunteeringId) {
        this.volunteeringIds.add(volunteeringId);
    }

    public void addOrganization(int organizationId) {
        this.myOrganizationIds.add(organizationId);
    }

    public void addVolunteeringToHistory(VolunteeringDTO volunteeringDTO) {
        VolunteeringInHistory hist = new VolunteeringInHistory(volunteeringDTO);
        if(!this.volunteeringsInHistory.contains(hist)) {
            this.volunteeringsInHistory.add(hist);
        }
    }

    public String getUsername() {
        return username;
    }

    public List<String> getEmails() {
        return emails;
    }

    public String getName() {
        return name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public List<String> getPreferredCategories() {
        return preferredCategories;
    }

    public List<Integer> getVolunteeringIds() {
        return volunteeringIds;
    }

    public List<VolunteeringDTO> getVolunteeringsInHistory() {
        return volunteeringsInHistory.stream().map(hist ->hist.toDTO()).toList();
    }

    public List<Integer> getMyOrganizationIds() {
        return myOrganizationIds;
    }

    public List<String> getSkills() {
        return skills;
    }

    public boolean isStudent() {
        return isStudent;
    }

    public void removeVolunteering(int volunteeringId) {
        if(volunteeringIds.contains(volunteeringId)){
            volunteeringIds.remove(Integer.valueOf(volunteeringId));
        }
    }

    public void removeOrganization(int organizationId) {
        if(myOrganizationIds.contains(organizationId)){
            myOrganizationIds.remove(Integer.valueOf(organizationId));
        }
    }

    public Set<String> getFcmTokens() {
        return fcmTokens;
    }

    public void addToken(String fcmToken) {
        if(fcmTokens == null){
            fcmTokens = new HashSet<>();
        }
        fcmTokens.add(fcmToken);
    }

    public void expireToken(String fcmToken) {
        if(fcmTokens != null){
            fcmTokens.remove(fcmToken);
        }
    }

    public boolean getLeaderboard() {
        return this.leaderboard;
    }

    public void setLeaderboard(boolean leaderboard) {
        this.leaderboard = leaderboard;
    }

    public void uploadCV(byte[] cv) {
        this.cv = cv;
    }

    public byte[] getCv() {
        return cv;
    }

    public void setCv(byte[] cv) {
        this.cv = cv;
    }

    public boolean isRemindActivity() {
        return remindActivity;
    }

    public void setRemindActivity(boolean remindActivity) {
        this.remindActivity = remindActivity;
    }

    public boolean isNotifyRecommendations() {
        return notifyRecommendations;
    }

    public void setNotifyRecommendations(boolean notifyRecommendations) {
        this.notifyRecommendations = notifyRecommendations;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}
