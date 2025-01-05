package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.utils.OrganizationErrors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationRepositoryUnitTest {
    private static MemoryOrganizationRepository memoryOrganizationRepository;
    private static DBOrganizationRepository dbOrganizationRepository;
    private int memOrgId, dbOrgId, orgId;

    @Mock
    private Organization organizationMock1;

    @Mock
    private Organization organizationMock2;

    @BeforeAll
    static void setUpBeforeAll() {
        memoryOrganizationRepository = new MemoryOrganizationRepository();
        dbOrganizationRepository = new DBOrganizationRepository();
    }

    @BeforeEach
    void setUpBeforeEach() {
        MockitoAnnotations.openMocks(OrganizationRepositoryUnitTest.class);

        memOrgId = memoryOrganizationRepository.createOrganization(organizationMock1);
        dbOrgId = dbOrganizationRepository.createOrganization(organizationMock1);
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
        List<Organization> expectedBeforeAdd = new ArrayList<>();
        expectedBeforeAdd.add(organizationMock1);

        List<Organization> expectedAfterAdd = new ArrayList<>();
        expectedAfterAdd.add(organizationMock1);
        expectedAfterAdd.add(organizationMock2);

        List<Organization> resBeforeAdd = organizationRepository.getAllOrganizations();
        assertEquals(expectedBeforeAdd, resBeforeAdd);

        organizationRepository.createOrganization(organizationMock2);
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
        this.orgId = organizationRepository == memoryOrganizationRepository ? memOrgId : dbOrgId;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.removeOrganization(orgId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(orgId + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingOrganizationAndValidFields_whenEditOrganization_thenEdit(OrganizationRepository organizationRepository) {
        this.orgId = organizationRepository == memoryOrganizationRepository ? memOrgId : dbOrgId;

        doNothing().when(organizationMock1).editOrganization(any(), any(), any(), any());
        assertDoesNotThrow(() -> organizationRepository.editOrganization(orgId, "Magen David Adom", "description", "0547612954", "mada@gmail.com"));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingOrganizationAndNonValidFields_whenEditOrganization_thenThrowException(OrganizationRepository organizationRepository) {
        this.orgId = organizationRepository == memoryOrganizationRepository ? memOrgId : dbOrgId;

        doThrow(new IllegalArgumentException()).when(organizationMock1).editOrganization(any(), any(), any(), any());
        assertThrows(IllegalArgumentException.class, () -> organizationRepository.editOrganization(orgId, "Invalid", "Invalid", "Invalid", "Invalid"));
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
        this.orgId = organizationRepository == memoryOrganizationRepository ? memOrgId : dbOrgId;
        final Organization[] organization = new Organization[1];
        assertDoesNotThrow(() -> organization[0] = organizationRepository.getOrganization(orgId));
        assertEquals(organizationMock1, organization[0]);
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
        List<Organization> expected = new ArrayList<>();
        expected.add(organizationMock1);
        List<Organization> res = organizationRepository.getAllOrganizations();
        assertEquals(expected, res);
    }
}