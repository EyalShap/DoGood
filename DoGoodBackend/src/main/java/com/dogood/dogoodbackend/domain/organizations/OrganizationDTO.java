package com.dogood.dogoodbackend.domain.organizations;

import java.util.List;
import java.util.Objects;

public class OrganizationDTO {
    private int id;
    private String name;
    private String description;
    private String phoneNumber;
    private String email;
    private List<Integer> volunteeringIds;
    private List<String> managerUsernames;
    private String founderUsername;

    public OrganizationDTO(int id, String name, String description, String phoneNumber, String email, List<Integer> volunteeringIds, List<String> managerUsernames, String founderUsername) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.volunteeringIds = volunteeringIds;
        this.managerUsernames = managerUsernames;
        this.founderUsername = founderUsername;
    }

    public OrganizationDTO(Organization organization) {
        this.id = organization.getId();
        this.name = organization.getName();
        this.description = organization.getDescription();
        this.phoneNumber = organization.getPhoneNumber();
        this.email = organization.getEmail();
        this.volunteeringIds = organization.getVolunteeringIds();
        this.managerUsernames = organization.getManagerUsernames();
        this.founderUsername = organization.getFounderUsername();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationDTO that = (OrganizationDTO) o;
        return id == that.id && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(email, that.email) && Objects.equals(volunteeringIds, that.volunteeringIds) && Objects.equals(managerUsernames, that.managerUsernames) && Objects.equals(founderUsername, that.founderUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, phoneNumber, email, volunteeringIds, managerUsernames, founderUsername);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setVolunteeringIds(List<Integer> volunteeringIds) {
        this.volunteeringIds = volunteeringIds;
    }

    public void setManagerUsernames(List<String> managerUsernames) {
        this.managerUsernames = managerUsernames;
    }

    public void setFounderUsername(String founderUsername) {
        this.founderUsername = founderUsername;
    }
}
