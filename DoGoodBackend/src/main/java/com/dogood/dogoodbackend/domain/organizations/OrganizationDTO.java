package com.dogood.dogoodbackend.domain.organizations;

import java.util.List;

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
}
