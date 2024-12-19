package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.utils.OrganizationErrors;

import java.util.ArrayList;
import java.util.List;

import static com.dogood.dogoodbackend.utils.ValidateFields.*;

public class Organization {
    private int id;
    private String name;
    private String description;
    private String phoneNumber;
    private String email;
    private List<Integer> volunteeringIds;
    private List<String> managerUsernames;
    private String founderUsername;

    public Organization(int id, String name, String description, String phoneNumber, String email, String actor) {
        String isValidOrg = isValid(id, name, description, phoneNumber, email);
        if(isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.id = id;
        this.name = name;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.volunteeringIds = new ArrayList<>();
        this.managerUsernames = new ArrayList<>();
        this.managerUsernames.add(actor); // adding founder also as manager so if he sets another founder he is still a manager
        this.founderUsername = actor;
    }

    private String isValid(int id, String name, String description, String phoneNumber, String email) {
        StringBuilder res = new StringBuilder();
        if(id < 0) {
            res.append(String.format("Invalid id: %d.\n", id));
        }
        if(!isValidText(name, 2, 50)) {
            res.append(String.format("Invalid organization name: %s.\n", name));
        }
        if(!isValidText(description, 50, 300)) {
            res.append(String.format("Invalid organization description: %s.\n", description));
        }
        if(!isValidPhoneNumber(phoneNumber)) {
            res.append(String.format("Invalid phone number: %s.\n", phoneNumber));
        }
        if(!isValidEmail(email)) {
            res.append(String.format("Invalid email: %s.", email));
        }
        return res.toString();
    }

    public boolean isManager(String username) {
        return managerUsernames.contains(username);
    }

    public boolean isFounder(String username) {
        return founderUsername.equals(username);
    }

    public void editOrganization(String newName, String newDescription, String newPhoneNumber, String newEmail) {
        String isValidOrg = isValid(id, name, description, phoneNumber, email);
        if(isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.name = newName;
        this.description = newDescription;
        this.phoneNumber = newPhoneNumber;
        this.email = newEmail;
    }

    public void addManager(String addedManagerUsername) {
        managerUsernames.add(addedManagerUsername);
    }

    public void removeManager(String username) {
        if(!isManager(username)) {
            throw new IllegalArgumentException(OrganizationErrors.makeUserIsNotAManagerError(username, name));
        }

        managerUsernames.remove(username);
    }

    public void resign(String actor) {
        // assuming a founder can not resign

        if(isFounder(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeFounderCanNotResignError(actor, name));
        }

        removeManager(actor);
    }

    public void setFounder(String newFounderUsername) {
        // so the founder can not set random people as founders, only ones that confirmed being a manager in the organization
        if(!isManager(newFounderUsername)) {
            throw new IllegalArgumentException(OrganizationErrors.makeUserIsNotAManagerError(newFounderUsername, name));
        }

        this.founderUsername = newFounderUsername;
    }

    public void addVolunteering(int volunteeringId) {
        if(volunteeringIds.contains(volunteeringId)) {
            throw new IllegalArgumentException(OrganizationErrors.makeVolunteeringAlreadyExistsError(volunteeringId, name));
        }
        this.volunteeringIds.add(volunteeringId);
    }

    public void removeVolunteering(int volunteeringId) {
        if(!volunteeringIds.contains(volunteeringId)) {
            throw new IllegalArgumentException(OrganizationErrors.makeVolunteeringDoesNotExistsError(volunteeringId, name));
        }
        this.volunteeringIds.remove(volunteeringId);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public List<Integer> getVolunteeringIds() {
        return volunteeringIds;
    }

    public List<String> getManagerUsernames() {
        return managerUsernames;
    }

    public String getFounderUsername() {
        return founderUsername;
    }
}
