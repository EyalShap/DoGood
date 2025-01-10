package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.utils.OrganizationErrors;

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
    public int getNextOrganizationId() {
        return nextOrganizationId;
    }

    @Override
    public int createOrganization(Organization organization) {
        if(organizations.containsKey(nextOrganizationId)) {
            throw new IllegalArgumentException(OrganizationErrors.makeOrganizationIdAlreadyExistsError(nextOrganizationId));
        }
        if(organization == null) {
            throw new IllegalArgumentException(OrganizationErrors.makeInvalidOrganizationError());
        }

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
    public Organization getOrganization(int organizationId) {
        if(!organizations.containsKey(organizationId)) {
            throw new IllegalArgumentException(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId));
        }
        return organizations.get(organizationId);
    }

    @Override
    public List<Organization> getAllOrganizations() {
        return new ArrayList<>(organizations.values());
    }
}
