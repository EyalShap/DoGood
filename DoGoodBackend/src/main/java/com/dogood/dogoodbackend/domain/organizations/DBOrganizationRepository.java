package com.dogood.dogoodbackend.domain.organizations;

import java.util.List;

public class DBOrganizationRepository implements OrganizationRepository{
    @Override
    public int createOrganization(String name, String description, String phoneNumber, String email, String actor) {
        //TODO
        return 0;
    }

    @Override
    public void removeOrganization(int organizationId) {
        //TODO
    }

    @Override
    public void editOrganization(int organizationId, String name, String description, String phoneNumber, String email) {
        //TODO
    }

    @Override
    public Organization getOrganization(int organizationId) {
        //TODO
        return null;
    }

    @Override
    public List<Organization> getAllOrganizations() {
        //TODO
        return null;
    }
}
