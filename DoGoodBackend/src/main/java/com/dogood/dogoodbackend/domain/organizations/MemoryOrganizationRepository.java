package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.jparepos.OrganizationJPA;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryOrganizationRepository implements OrganizationRepository{
    private int nextOrganizationId;
    private Map<Integer, Organization> organizations;

    public MemoryOrganizationRepository() {
        this.nextOrganizationId = 0;
        this.organizations = new HashMap<>();
    }

    @Override
    public int createOrganization(String name, String description, String phoneNumber, String email, String actor) {
        if(organizations.containsKey(nextOrganizationId)) {
            throw new IllegalArgumentException(OrganizationErrors.makeOrganizationIdAlreadyExistsError(nextOrganizationId));
        }

        Organization organization = new Organization(nextOrganizationId, name, description, phoneNumber, email, actor);
        organizations.put(nextOrganizationId, organization);
        nextOrganizationId++;
        return nextOrganizationId - 1;
    }

    @Override
    public void removeOrganization(int organizationId) {
        if(!organizations.containsKey(organizationId)) {
            throw new IllegalArgumentException(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId));
        }

        organizations.remove(organizationId);
    }

    @Override
    public void editOrganization(int organizationId, String name, String description, String phoneNumber, String email) {
        Organization toEdit = getOrganization(organizationId); // will throw exception if does not exist
        toEdit.editOrganization(name, description, phoneNumber, email);
    }

    @Override
    public void setVolunteeringIds(int organizationId, List<Integer> volunteeringIds) {
        Organization toSet = getOrganization(organizationId);
        toSet.setVolunteeringIds(volunteeringIds);
    }

    @Override
    public void setManagers(int organizationId, List<String> managers) {
        Organization toSet = getOrganization(organizationId);
        toSet.setManagers(managers);
    }

    @Override
    public void setFounder(int organizationId, String newFounder) {
        Organization toSet = getOrganization(organizationId);
        toSet.setFounder(newFounder);
    }

    @Override
    public void setImages(int organizationId, List<String> images) {
        Organization toSet = getOrganization(organizationId);
        toSet.setImagePaths(images);
    }

    @Override
    public void uploadSignature(int organizationId, String actor, MultipartFile signature) {
        Organization organization = getOrganization(organizationId);

        try {
            byte[] signatureBytes = signature != null ? signature.getBytes() : null;
            organization.uploadSignature(actor, signatureBytes);
        }
        catch (IOException exception) {
            throw new IllegalArgumentException("Problem uploading signature.");
        }
    }

    @Override
    public byte[] getSignature(int organizationId, String actor) {
        return getOrganization(organizationId).getSignature(actor);
    }

    private Organization getOrganization(int organizationId) {
        if(!organizations.containsKey(organizationId)) {
            throw new IllegalArgumentException(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId));
        }
        return organizations.get(organizationId);
    }

    @Override
    public Organization getOrganizationForRead(int organizationId) {
        return getOrganization(organizationId);
    }

    @Override
    public Organization getOrganizationForWrite(int organizationId) {
        return getOrganization(organizationId);
    }

    @Override
    public List<Organization> getAllOrganizations() {
        return new ArrayList<>(organizations.values());
    }

    @Override
    public void clear() {
        this.nextOrganizationId = 0;
        this.organizations = new HashMap<>();
    }
}
