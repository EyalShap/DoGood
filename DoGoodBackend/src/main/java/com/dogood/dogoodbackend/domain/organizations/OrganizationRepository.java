package com.dogood.dogoodbackend.domain.organizations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface OrganizationRepository {
    public int createOrganization(String name, String description, String phoneNumber, String email, String actor);
    public void removeOrganization(int organizationId);
    public void editOrganization(int organizationId, String name, String description, String phoneNumber, String email);
    public void setVolunteeringIds(int organizationId, List<Integer> volunteeringIds);
    public void setManagers(int organizationId, List<String> managers);
    public void setFounder(int organizationId, String newFounder);
    public Organization getOrganization(int organizationId);
    public List<Organization> getAllOrganizations();

    public default List<OrganizationDTO> getAllOrganizationDTOs() {
        List<Organization> organizations = getAllOrganizations();
        List<OrganizationDTO> organizationDTOS = organizations.stream()
                .map(org -> new OrganizationDTO(org))
                .collect(Collectors.toList());
        return organizationDTOS;
    }
}
