package com.dogood.dogoodbackend.domain.organizations;

import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
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
    public void setImages(int organizationId, List<String> images);
    public void uploadSignature(int organizationId, String actor, MultipartFile signature);
    public byte[] getSignature(int organizationId, String actor);
    public Organization getOrganization(int organizationId);
    public List<Organization> getAllOrganizations();
    public void clear();

    public default List<OrganizationDTO> getAllOrganizationDTOs() {
        List<Organization> organizations = getAllOrganizations();
        List<OrganizationDTO> organizationDTOS = organizations.stream()
                .map(org -> new OrganizationDTO(org))
                .collect(Collectors.toList());
        return organizationDTOS;
    }
}
