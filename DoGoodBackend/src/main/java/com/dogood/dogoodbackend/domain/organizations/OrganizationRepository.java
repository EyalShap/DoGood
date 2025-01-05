package com.dogood.dogoodbackend.domain.organizations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface OrganizationRepository {
    public int getNextOrganizationId();
    public int createOrganization(Organization organization);
    public void removeOrganization(int organizationId);
    public void editOrganization(int organizationId, String name, String description, String phoneNumber, String email);
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
