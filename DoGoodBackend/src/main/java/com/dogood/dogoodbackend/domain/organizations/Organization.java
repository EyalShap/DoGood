package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.utils.OrganizationErrors;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.dogood.dogoodbackend.utils.ValidateFields.*;

@Entity
@Table(name = "organizations")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_id")
    private int id;

    @Column(name = "organization_name")
    private String name;

    @Column(name = "organization_description")
    private String description;

    @Column(name = "organization_phone_number")
    private String phoneNumber;

    @Column(name = "organization_email")
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_volunteering_ids", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "volunteering_id")
    private List<Integer> volunteeringIds;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "organization_managers", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "manager_username")
    private List<String> managerUsernames;

    @Column(name = "organization_founder")
    private String founderUsername;

    public Organization() {}

    public Organization(int id, String name, String description, String phoneNumber, String email, String actor) {
        this.id = id;
        setFields(name, description, phoneNumber, email, actor);
    }

    public Organization(String name, String description, String phoneNumber, String email, String actor) {
        setFields(name, description, phoneNumber, email, actor);
    }

    private void setFields(String name, String description, String phoneNumber, String email, String actor) {
        String isValidOrg = isValid(name, description, phoneNumber, email);
        if(isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.name = name;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.volunteeringIds = new ArrayList<>();
        this.managerUsernames = new ArrayList<>();
        this.managerUsernames.add(actor); // adding founder also as manager so if he sets another founder he is still a manager
        this.founderUsername = actor;
    }

    private String isValid(String name, String description, String phoneNumber, String email) {
        StringBuilder res = new StringBuilder();
        if(!isValidText(name, 2, 50)) {
            res.append(String.format("Invalid organization name: %s.\n", name));
        }
        if(!isValidText(description, 2, 300)) {
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
        String isValidOrg = isValid(newName, newDescription, newPhoneNumber, newEmail);
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
        if(isFounder(username)) {
            throw new IllegalArgumentException(OrganizationErrors.makeFounderCanNotBeRemovedError(username, name));
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
        boolean removed = this.volunteeringIds.remove(Integer.valueOf(volunteeringId));
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

    public void setVolunteeringIds(List<Integer> volunteeringIds) {
        this.volunteeringIds = volunteeringIds;
    }

    public void setManagers(List<String> managers) {
        this.managerUsernames = managers;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization that = (Organization) o;
        return id == that.id && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(email, that.email) && Objects.equals(volunteeringIds, that.volunteeringIds) && Objects.equals(managerUsernames, that.managerUsernames) && Objects.equals(founderUsername, that.founderUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, phoneNumber, email, volunteeringIds, managerUsernames, founderUsername);
    }




}
