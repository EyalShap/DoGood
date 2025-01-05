package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.utils.OrganizationErrors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class OrganizationRepositoryIntegrationTest {
    private static MemoryOrganizationRepository memoryOrganizationRepository;
    private static DBOrganizationRepository dbOrganizationRepository;
    private int memOrgId, dbOrgId, orgId;
    private Organization memOrganization, dbOrganization;
    private final String name = "Magen David Adom";
    private final String description = "Magen David Adom is Israel's national emergency medical and blood services organization.";
    private final String phoneNumber = "0548124087";
    private final String email = "mada@gmail.com";
    private final String actor1 = "TheDoctor";


    @BeforeAll
    static void setUpBeforeAll() {
        memoryOrganizationRepository = new MemoryOrganizationRepository();
        dbOrganizationRepository = new DBOrganizationRepository();
    }

    @BeforeEach
    void setUpBeforeEach() {
        memOrgId = memoryOrganizationRepository.getNextOrganizationId();
        dbOrgId = dbOrganizationRepository.getNextOrganizationId();
        this.memOrganization = new Organization(memOrgId, name, description, phoneNumber, email, actor1);
        this.dbOrganization = new Organization(dbOrgId, name, description, phoneNumber, email, actor1);
        memoryOrganizationRepository.createOrganization(memOrganization);
        dbOrgId = dbOrganizationRepository.createOrganization(dbOrganization);
    }

    @AfterEach
    void afterEach() {
        try {
            Organization organization = memoryOrganizationRepository.getOrganization(memOrgId);
            memoryOrganizationRepository.removeOrganization(memOrgId);
        }
        catch (Exception e) {

        }

        try {
            Organization organization = dbOrganizationRepository.getOrganization(dbOrgId);
            memoryOrganizationRepository.removeOrganization(memOrgId);
        }
        catch (Exception e) {

        }

    }

    static Stream<OrganizationRepository> repoProvider() {
        return Stream.of(memoryOrganizationRepository);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenValidFields_whenCreateOrganization_thenCreate(OrganizationRepository organizationRepository) {
        Organization organization1 = organizationRepository == memoryOrganizationRepository ? memOrganization : dbOrganization;

        int orgId2 = organizationRepository.getNextOrganizationId();
        Organization organization2 = new Organization(orgId2, "Magen David Kachol", "Water services", "0548195544", "madk@gmail.com", actor1);

        List<Organization> expectedBeforeAdd = new ArrayList<>();
        expectedBeforeAdd.add(organization1);

        List<Organization> expectedAfterAdd = new ArrayList<>();
        expectedAfterAdd.add(organization1);
        expectedAfterAdd.add(organization2);

        List<Organization> resBeforeAdd = organizationRepository.getAllOrganizations();
        assertEquals(expectedBeforeAdd, resBeforeAdd);

        organizationRepository.createOrganization(organization2);
        List<Organization> resAfterAdd = organizationRepository.getAllOrganizations();
        assertEquals(expectedAfterAdd, resAfterAdd);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenRemoveOrganization_thenRemove(OrganizationRepository organizationRepository) {
        this.orgId = organizationRepository == memoryOrganizationRepository ? memOrgId : dbOrgId;

        assertDoesNotThrow(() -> organizationRepository.removeOrganization(orgId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.getOrganization(orgId);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(orgId), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenRemoveOrganization_thenThrowException(OrganizationRepository organizationRepository) {
        int newId = organizationRepository.getNextOrganizationId();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.removeOrganization(newId);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(newId), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingOrganizationAndValidFields_whenEditOrganization_thenEdit(OrganizationRepository organizationRepository) {
        this.orgId = organizationRepository == memoryOrganizationRepository ? memOrgId : dbOrgId;

        assertDoesNotThrow(() -> organizationRepository.editOrganization(orgId, "Magen David Kachol", "water services", "0547612954", "madk@gmail.com"));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingOrganizationAndNonValidFields_whenEditOrganization_thenThrowException(OrganizationRepository organizationRepository) {
        this.orgId = organizationRepository == memoryOrganizationRepository ? memOrgId : dbOrgId;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> organizationRepository.editOrganization(orgId, "", "", "", ""));
        StringBuilder expectedError = new StringBuilder();
        expectedError
                .append("Invalid organization name: .\n")
                .append("Invalid organization description: .\n")
                .append("Invalid phone number: .\n")
                .append("Invalid email: .");

        assertEquals(expectedError.toString(), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingOrganization_whenEditOrganization_thenThrowException(OrganizationRepository organizationRepository) {
        this.orgId = organizationRepository == memoryOrganizationRepository ? memOrgId : dbOrgId;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.editOrganization(orgId + 1, "Magen David Adom", "description", "0541970256", "mada@gmail.com");
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(orgId + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenGetOrganization_thenNoThrownException(OrganizationRepository organizationRepository) {
        Organization expectedOrganization = organizationRepository == memoryOrganizationRepository ? memOrganization : dbOrganization;
        final Organization[] organization = new Organization[1];
        assertDoesNotThrow(() -> organization[0] = organizationRepository.getOrganization(orgId));
        assertEquals(expectedOrganization, organization[0]);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenGetOrganization_thenThrowException(OrganizationRepository organizationRepository) {
        this.orgId = organizationRepository == memoryOrganizationRepository ? memOrgId : dbOrgId;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.getOrganization(orgId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(orgId + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void getAllOrganizations(OrganizationRepository organizationRepository) {
        Organization expectedOrganization = organizationRepository == memoryOrganizationRepository ? memOrganization : dbOrganization;
        List<Organization> expected = new ArrayList<>();
        expected.add(expectedOrganization);
        List<Organization> res = organizationRepository.getAllOrganizations();
        assertEquals(expected, res);
    }
}