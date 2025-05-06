package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.utils.OrganizationErrors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
abstract class AbstractOrganizationRepositoryTest {
    private OrganizationRepository organizationRepository;

    private int organizationId;
    private Organization organization;
    private final String name = "Magen David Adom";
    private final String description = "Magen David Adom is Israel's national emergency medical and blood services organization.";
    private final String phoneNumber = "0548124087";
    private final String email = "mada@gmail.com";
    private final String actor1 = "TheDoctor";

    protected abstract OrganizationRepository createRepository();

    @BeforeEach
    void setUpBeforeEach() {
        organizationRepository = createRepository();
        this.organizationId = organizationRepository.createOrganization(name, description, phoneNumber, email, actor1);
        this.organization = new Organization(organizationId, name, description, phoneNumber, email, actor1);
    }

    @AfterEach
    void afterEach() {
        organizationRepository.clear();
    }

    @Test
    void givenValidFields_whenCreateOrganization_thenCreate() {
        List<Organization> expectedBeforeAdd = new ArrayList<>();
        expectedBeforeAdd.add(organization);

        List<Organization> resBeforeAdd = organizationRepository.getAllOrganizations();
        assertEquals(expectedBeforeAdd, resBeforeAdd);
        int orgId2 = organizationRepository.createOrganization("Magen David Kachol", "Water services", "0548195544", "madk@gmail.com", actor1);
        Organization organization2 = new Organization(orgId2, "Magen David Kachol", "Water services", "0548195544", "madk@gmail.com", actor1);

        List<Organization> expectedAfterAdd = new ArrayList<>();
        expectedAfterAdd.add(organization);
        expectedAfterAdd.add(organization2);
        List<Organization> resAfterAdd = organizationRepository.getAllOrganizations();
        assertEquals(expectedAfterAdd, resAfterAdd);
    }

    @Test
    void givenInvalidFields_whenCreateOrganization_thenThrowException() {
        List<Organization> expected = new ArrayList<>();
        expected.add(organization);

        List<Organization> resBeforeAdd = organizationRepository.getAllOrganizations();
        assertEquals(expected, resBeforeAdd);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> organizationRepository.createOrganization("", "Water services", "0548195544", "madk@gmail.com", actor1));
        String expectedError = "Invalid organization name: .\n";

        assertEquals(expectedError, exception.getMessage());

        List<Organization> resAfterAdd = organizationRepository.getAllOrganizations();
        assertEquals(expected, resAfterAdd);
    }

    @Test
    void givenExistingId_whenRemoveOrganization_thenRemove() {
        assertDoesNotThrow(() -> organizationRepository.removeOrganization(organizationId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.getOrganization(organizationId);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId), exception.getMessage());
    }

    @Test
    void givenNonExistingId_whenRemoveOrganization_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.removeOrganization(organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenExistingOrganizationAndValidFields_whenEditOrganization_thenEdit() {
        assertDoesNotThrow(() -> organizationRepository.editOrganization(organizationId, "Magen David Kachol", "water services", "0547612954", "madk@gmail.com"));
    }

    @Test
    void givenExistingOrganizationAndNonValidFields_whenEditOrganization_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> organizationRepository.editOrganization(organizationId, "", "", "", ""));
        StringBuilder expectedError = new StringBuilder();
        expectedError
                .append("Invalid organization name: .\n")
                .append("Invalid organization description: .\n")
                .append("Invalid phone number: .\n")
                .append("Invalid email: .");

        assertEquals(expectedError.toString(), exception.getMessage());
    }

    @Test
    void givenNonExistingOrganization_whenEditOrganization_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.editOrganization(organizationId + 1, "Magen David Adom", "description", "0541970256", "mada@gmail.com");
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenExistingId_whenSetVolunteeringIds_thenSet() {
        List<Integer> resBefore = organizationRepository.getOrganization(organizationId).getVolunteeringIds();
        assertEquals(new HashSet<>(), new HashSet<>(resBefore));

        List<Integer> volunteeringIds = List.of(1, 2, 3);
        organizationRepository.setVolunteeringIds(organizationId, volunteeringIds);

        List<Integer> resAfter = organizationRepository.getOrganization(organizationId).getVolunteeringIds();
        assertEquals(new HashSet<>(volunteeringIds), new HashSet<>(resAfter));
    }

    @Test
    void givenNonExistingId_whenSetVolunteeringIds_thThrowException() {
        List<Integer> resBefore = organizationRepository.getOrganization(organizationId).getVolunteeringIds();
        assertEquals(new HashSet<>(), new HashSet<>(resBefore));

        List<Integer> volunteeringIds = List.of(1, 2, 3);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.setVolunteeringIds(organizationId + 1, volunteeringIds);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());

        List<Integer> resAfter = organizationRepository.getOrganization(organizationId).getVolunteeringIds();
        assertEquals(new HashSet<>(), new HashSet<>(resAfter));
    }

    @Test
    void givenExistingId_whenSetManagers_thenSet() {
        List<String> expectedBefore = List.of(actor1);
        List<String> resBefore = organizationRepository.getOrganization(organizationId).getManagerUsernames();
        assertEquals(new HashSet<>(expectedBefore), new HashSet<>(resBefore));

        List<String> managers = List.of(actor1, "actor2", "actor3");
        organizationRepository.setManagers(organizationId, managers);

        List<String> resAfter = organizationRepository.getOrganization(organizationId).getManagerUsernames();
        assertEquals(new HashSet<>(managers), new HashSet<>(resAfter));
    }

    @Test
    void givenNonExistingId_whenSetManagers_thenThrowException() {
        List<String> managers = List.of(actor1, "actor2", "actor3");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.setManagers(organizationId + 1, managers);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenExistingIdAndManager_whenSetFounder_thenSet() {
        String newFounder = "actor2";
        organizationRepository.setManagers(organizationId, List.of(actor1, newFounder));

        String resBefore = organizationRepository.getOrganization(organizationId).getFounderUsername();
        assertEquals(actor1, resBefore);

        organizationRepository.setFounder(organizationId, newFounder);

        String resAfter = organizationRepository.getOrganization(organizationId).getFounderUsername();
        assertEquals(newFounder, resAfter);
    }

    @Test
    void givenNonExistingId_whenSetFounder_thThrowException() {
        String newFounder = "actor2";
        organizationRepository.setManagers(organizationId, List.of(actor1, newFounder));

        String resBefore = organizationRepository.getOrganization(organizationId).getFounderUsername();
        assertEquals(actor1, resBefore);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.setFounder(organizationId + 1, newFounder);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());

        String resAfter = organizationRepository.getOrganization(organizationId).getFounderUsername();
        assertEquals(actor1, resAfter);
    }

    @Test
    void givenNonManager_whenSetFounder_thThrowException() {
        String resBefore = organizationRepository.getOrganization(organizationId).getFounderUsername();
        assertEquals(actor1, resBefore);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.setFounder(organizationId, "actor2");
        });
        assertEquals(OrganizationErrors.makeUserIsNotAManagerError("actor2", name), exception.getMessage());

        String resAfter = organizationRepository.getOrganization(organizationId).getFounderUsername();
        assertEquals(actor1, resAfter);
    }

    @Test
    void givenExistingId_whenSetImages_thenSet() {
        List<String> resBefore = organizationRepository.getOrganization(organizationId).getImagePaths();
        assertEquals(0, resBefore.size());

        List<String> images = List.of("path");
        organizationRepository.setImages(organizationId, images);

        List<String> resAfter = organizationRepository.getOrganization(organizationId).getImagePaths();
        assertEquals(1, resAfter.size());
        assertEquals("path", resAfter.get(0));
    }

    @Test
    void givenNonExistingId_whenSetImages_thThrowException() {
        List<String> resBefore = organizationRepository.getOrganization(organizationId).getImagePaths();
        assertEquals(0, resBefore.size());

        List<String> images = List.of("path");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.setImages(organizationId + 1, images);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());

        List<Integer> resAfter = organizationRepository.getOrganization(organizationId).getVolunteeringIds();
        assertEquals(0, resAfter.size());
    }

    @Test
    void givenExistingIdAndValidSignature_whenUploadSignature_thenUpload() throws IOException {
        byte[] resBefore = organizationRepository.getOrganization(organizationId).getSignature();
        assertNull(resBefore);

        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);
        MockMultipartFile signature = new MockMultipartFile("file","signature.png","image/png", randomBytes);

        organizationRepository.uploadSignature(organizationId, actor1, signature);

        byte[] resAfter = organizationRepository.getOrganization(organizationId).getSignature();
        assertArrayEquals(signature.getBytes(), resAfter);
    }

    @Test
    void givenNonExistingId_whenUploadSignature_thThrowException() {
        byte[] resBefore = organizationRepository.getOrganization(organizationId).getSignature();
        assertNull(resBefore);

        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);
        MockMultipartFile signature = new MockMultipartFile("file","signature.png","image/png", randomBytes);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.uploadSignature(organizationId + 1, actor1, signature);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());

        byte[] resAfter = organizationRepository.getOrganization(organizationId).getSignature();
        assertNull(resAfter);
    }

    @Test
    void givenNonFounder_whenUploadSignature_thenThrowException() {
        organizationRepository.setManagers(organizationId, List.of(actor1, "actor2"));

        byte[] resBefore = organizationRepository.getOrganization(organizationId).getSignature();
        assertNull(resBefore);

        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);
        MockMultipartFile signature = new MockMultipartFile("file","signature.png","image/png", randomBytes);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.uploadSignature(organizationId, "actor2", signature);
        });
        assertEquals("Only organization founder can upload signature.", exception.getMessage());

        byte[] resAfter = organizationRepository.getOrganization(organizationId).getSignature();
        assertNull(resAfter);
    }

    @Test
    void givenNullSignature_whenUploadSignature_thenThrowException() throws IOException {
        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);
        MockMultipartFile signature = new MockMultipartFile("file","signature.png","image/png", randomBytes);
        organizationRepository.uploadSignature(organizationId, actor1, signature);

        byte[] resBefore = organizationRepository.getOrganization(organizationId).getSignature();
        assertArrayEquals(signature.getBytes(), resBefore);

        organizationRepository.uploadSignature(organizationId, actor1, null);

        byte[] resAfter = organizationRepository.getOrganization(organizationId).getSignature();
        assertNull(resAfter);
    }

    @Test
    void givenManager_whenGetSignature_thenReturnSignature() {
        organizationRepository.setManagers(organizationId, List.of(actor1, "actor2"));

        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);
        MockMultipartFile signature = new MockMultipartFile("file","signature.png","image/png", randomBytes);

        organizationRepository.uploadSignature(organizationId, actor1, signature);
        organizationRepository.setManagers(organizationId, List.of(actor1, "actor2"));

        byte[] res1 = organizationRepository.getSignature(organizationId, actor1);
        byte[] res2 = organizationRepository.getSignature(organizationId, "actor2");

        assertArrayEquals(randomBytes, res1);
        assertArrayEquals(randomBytes, res2);
    }

    @Test
    void givenNonExistingId_whenGetSignature_thThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.getSignature(organizationId + 1, actor1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenNonManager_whenGetSignature_thenReturnSignature() {
        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);
        MockMultipartFile signature = new MockMultipartFile("file","signature.png","image/png", randomBytes);

        organizationRepository.uploadSignature(organizationId, actor1, signature);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.getSignature("actor2");
        });
        assertEquals("Only organization manager can get signature.", exception.getMessage());
    }

    @Test
    void givenExistingId_whenGetOrganization_thenNoThrownException() {
        final Organization[] organizationWrapper = new Organization[1];
        assertDoesNotThrow(() -> organizationWrapper[0] = organizationRepository.getOrganization(organizationId));
        assertEquals(organization, organizationWrapper[0]);
    }

    @Test
    void givenNonExistingId_whenGetOrganization_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationRepository.getOrganization(organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void getAllOrganizations() {
        List<Organization> expected = new ArrayList<>();
        expected.add(organization);
        List<Organization> res = organizationRepository.getAllOrganizations();
        assertEquals(expected, res);
    }

}