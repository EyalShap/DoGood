package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.jparepos.OrganizationJPA;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import jakarta.transaction.Transactional;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class OrganizationRepositoryUnitTest extends AbstractOrganizationRepositoryTest{

    @Override
    protected OrganizationRepository createRepository() {
        return new MemoryOrganizationRepository();
    }

    /*@BeforeEach
    void setUpBeforeEach() {
        MockitoAnnotations.openMocks(OrganizationRepositoryUnitTest.class);

        OrganizationJPA organizationJPA = applicationContext.getBean(OrganizationJPA.class);
        dbOrganizationRepository.setJPA(organizationJPA);
        organizationJPA.deleteAll();

        memOrgId = memoryOrganizationRepository.createOrganization("Organization", "Description", "0541987066", "org@gmail.com", "TheDoctor");
        dbOrgId = dbOrganizationRepository.createOrganization("Organization", "Description", "0541987066", "org@gmail.com", "TheDoctor");
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
    void givenValidFieldsOrganization_whenCreateOrganization_thenCreate(OrganizationRepository organizationRepository) {
        List<Organization> expectedBeforeAdd = new ArrayList<>();
        expectedBeforeAdd.add(organizationMock1);

        List<Organization> expectedAfterAdd = new ArrayList<>();
        expectedAfterAdd.add(organizationMock1);
        expectedAfterAdd.add(organizationMock2);

        List<Organization> resBeforeAdd = organizationRepository.getAllOrganizations();
        assertEquals(expectedBeforeAdd, resBeforeAdd);

        organizationRepository.createOrganization("NewOrganization", "NewDescription", "0541987067", "neworg@gmail.com", "TheDoctor");
        List<Organization> resAfterAdd = organizationRepository.getAllOrganizations();
        assertEquals(expectedAfterAdd, resAfterAdd);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenRemoveOrganization_thenRemove(OrganizationRepository organizationRepository) {
        setIdByRepo(organizationRepository);

        assertDoesNotThrow(() -> organizationRepository.removeOrganization(orgId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.getOrganization(orgId);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(orgId), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenRemoveOrganization_thenThrowException(OrganizationRepository organizationRepository) {
        setIdByRepo(organizationRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.removeOrganization(orgId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(orgId + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingOrganizationAndValidFields_whenEditOrganization_thenEdit(OrganizationRepository organizationRepository) {
        setIdByRepo(organizationRepository);

        //doNothing().when(organizationMock1).editOrganization(any(), any(), any(), any());
        assertDoesNotThrow(() -> organizationRepository.editOrganization(orgId, "Magen David Adom", "description", "0547612954", "mada@gmail.com"));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingOrganizationAndNonValidFields_whenEditOrganization_thenThrowException(OrganizationRepository organizationRepository) {
        setIdByRepo(organizationRepository);

        //doThrow(new IllegalArgumentException()).when(organizationMock1).editOrganization(any(), any(), any(), any());
        assertThrows(IllegalArgumentException.class, () -> organizationRepository.editOrganization(orgId, "Invalid", "Invalid", "Invalid", "Invalid"));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingOrganization_whenEditOrganization_thenThrowException(OrganizationRepository organizationRepository) {
        setIdByRepo(organizationRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.editOrganization(orgId + 1, "Magen David Adom", "description", "0541970256", "mada@gmail.com");
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(orgId + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenGetOrganization_thenNoThrownException(OrganizationRepository organizationRepository) {
        setIdByRepo(organizationRepository);
        final Organization[] organization = new Organization[1];
        assertDoesNotThrow(() -> organization[0] = organizationRepository.getOrganization(orgId));


        assertEquals(organizationMock1, organization[0]);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenGetOrganization_thenThrowException(OrganizationRepository organizationRepository) {
        setIdByRepo(organizationRepository);

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
    
    private void setIdByRepo(OrganizationRepository repository) {
        this.orgId = repository == memoryOrganizationRepository ? memOrgId : dbOrgId;
    }*/
}