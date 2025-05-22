package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.jparepos.OrganizationJPA;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Transactional
public class DBOrganizationRepository implements OrganizationRepository{
    private OrganizationJPA jpa;

    public DBOrganizationRepository(OrganizationJPA jpa) {
        this.jpa = jpa;
    }

    public DBOrganizationRepository() {}

    public void setJPA(OrganizationJPA jpa) {
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
    public void editOrganization(int organizationId, String name, String description, String phoneNumber, String email) {
        Organization toEdit = getOrganizationForWrite(organizationId); // will throw exception if does not exist
        toEdit.editOrganization(name, description, phoneNumber, email);
        jpa.save(toEdit);
    }

    @Override
    @Transactional
    public void setVolunteeringIds(int organizationId, List<Integer> volunteeringIds) {
        Organization toSet = getOrganizationForWrite(organizationId);
        toSet.setVolunteeringIds(volunteeringIds);
        jpa.save(toSet);
    }

    @Override
    @Transactional
    public void setManagers(int organizationId, List<String> managers) {
        Organization toSet = getOrganizationForWrite(organizationId);
        toSet.setManagers(managers);
        jpa.save(toSet);
    }

    @Override
    @Transactional
    public void setFounder(int organizationId, String newFounder) {
        Organization toSet = getOrganizationForWrite(organizationId);
        toSet.setFounder(newFounder);
        jpa.save(toSet);
    }

    @Override
    @Transactional
    public void setImages(int organizationId, List<String> images) {
        Organization toSet = getOrganizationForWrite(organizationId);
        toSet.setImagePaths(images);
        jpa.save(toSet);
    }

    @Override
    @Transactional
    public void uploadSignature(int organizationId, String actor, MultipartFile signature) {
        Organization organization = getOrganizationForWrite(organizationId);

        try {
            byte[] signatureBytes = signature != null ? signature.getBytes() : null;
            organization.uploadSignature(actor, signatureBytes);
            jpa.save(organization);
        }
        catch (IOException exception) {
            throw new IllegalArgumentException("Problem uploading signature.");
        }
    }

    @Override
    @Transactional
    public byte[] getSignature(int organizationId, String actor) {
        Organization toSet = getOrganizationForRead(organizationId);
        return toSet.getSignature(actor);
    }

    @Override
    public Organization getOrganizationForRead(int organizationId) {
        Optional<Organization> organization = jpa.findById(organizationId); // does not lock org
        if(!organization.isPresent()) {
            throw new IllegalArgumentException(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId));
        }

        Organization o = organization.get();
        return o;
    }

    @Override
    public Organization getOrganizationForWrite(int organizationId) {
        Optional<Organization> organization = jpa.findByIdForUpdate(organizationId); // locks org
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

    @Override
    public void clear() {
        jpa.deleteAll();
    }
}
