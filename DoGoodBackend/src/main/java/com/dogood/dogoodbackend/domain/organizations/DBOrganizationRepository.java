package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.jparepos.OrganizationJPA;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DBOrganizationRepository implements OrganizationRepository{
    private OrganizationJPA jpa;

    @PersistenceContext
    private EntityManager entityManager;

    public DBOrganizationRepository(OrganizationJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public int createOrganization(String name, String description, String phoneNumber, String email, String actor) {
        Organization organization = new Organization(name, description, phoneNumber, email, actor);
        jpa.save(organization);
        return organization.getId();
    }

    @Override
    public void removeOrganization(int organizationId) {
        if(!jpa.existsById(organizationId)) {
            throw new IllegalArgumentException(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId));
        }
        jpa.deleteById(organizationId);
    }

    @Override
    @Transactional
    public void editOrganization(int organizationId, String name, String description, String phoneNumber, String email) {
        Organization toEdit = getOrganization(organizationId); // will throw exception if does not exist
        toEdit.editOrganization(name, description, phoneNumber, email);
        jpa.save(toEdit);
    }

    @Override
    public void setVolunteeringIds(int organizationId, List<Integer> volunteeringIds) {
        Organization toSet = getOrganization(organizationId);
        toSet.setVolunteeringIds(volunteeringIds);
        jpa.save(toSet);
    }

    @Override
    public void setManagers(int organizationId, List<String> managers) {
        Organization toSet = getOrganization(organizationId);
        toSet.setManagers(managers);
        jpa.save(toSet);
    }

    @Override
    public void setFounder(int organizationId, String newFounder) {
        Organization toSet = getOrganization(organizationId);
        toSet.setFounder(newFounder);
        jpa.save(toSet);
    }

    @Override
    @Transactional
    public Organization getOrganization(int organizationId) {
        Optional<Organization> organization = jpa.findById(organizationId);
        if(!organization.isPresent()) {
            throw new IllegalArgumentException(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId));
        }

        Organization o = organization.get();
        return o;
    }

    @Override
    public List<Organization> getAllOrganizations() {
        return jpa.findAll();
    }
}
