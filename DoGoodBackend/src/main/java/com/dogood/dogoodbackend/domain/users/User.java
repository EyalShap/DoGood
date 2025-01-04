package com.dogood.dogoodbackend.domain.users;

import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User {
    private String username;
    private List<String> emails;
    private String name;
    private String passwordHash;
    private String phone;
    private Date birthDate;
//    private File/String CV/CVpath;
    private List<String> preferredCategories;
//    private notification preferences?
    private List<Integer> volunteeringIds;
    private List<VolunteeringDTO> volunteeringsInHistory;
    private List<Integer> myOrganizationIds;
    private List<String> skills;
    private boolean isStudent;
    private boolean isAdmin;

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
        this.volunteeringsInHistory = volunteeringsInHistory;
        this.myOrganizationIds = myOrganizationIds;
        this.skills = skills;
        this.isStudent = isStudent;
        this.isAdmin = isAdmin;
    }

    private boolean isValidUsername(String username) {
        return username != null && username.length() >= 4 && username.chars().allMatch(Character::isLetterOrDigit) && username.chars().anyMatch(Character::isLetter);
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public boolean checkPassword(String password) {
        return passwordHash.equals(Cryptography.hashString(password));
    }

    private static boolean checkStudentEmail(String email) {
        return true; // TODO: implement by format
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }

    public void updateFields(List<String> emails, String name, String password, String phoneNumber) {
        this.emails = emails;
        this.name = name;
        this.passwordHash = Cryptography.hashString(password);
        this.phone = phoneNumber;
    }
}
