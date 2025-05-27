package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.utils.OrganizationErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class OrganizationUnitTest {
    private final int id = 0;
    private final String name = "Magen David Adom";
    private final String description = "Magen David Adom is Israel's national emergency medical and blood services organization.";
    private final String phoneNumber = "0548124087";
    private final String email = "mada@gmail.com";
    private final String actor1 = "TheDoctor";
    private final String actor2 = "NotTheDoctor";
    private final String actor3 = "NotNotTheDoctor";
    private final List<String> managers = List.of(actor1);
    private final List<Integer> volunteerings = List.of(0);
    private Organization organization;

    @BeforeEach
    void setUp() {
        this.organization = new Organization(id, name, description, phoneNumber, email, actor1);
        this.organization.addVolunteering(0);
    }

    @Test
    void whenIsManager_givenManager_thenReturnTrue() {
        boolean isManager = organization.isManager(actor1);
        assertTrue(isManager);
    }

    @Test
    void whenIsManager_givenNotManager_thenReturnFalse() {
        boolean isManager = organization.isManager(actor2);
        assertFalse(isManager);
    }

    @Test
    void whenIsFounder_givenFounder_thenReturnTrue() {
        boolean isFounder = organization.isFounder(actor1);
        assertTrue(isFounder);
    }

    @Test
    void whenIsFounder_givenManagerNotFounder_thenReturnFalse() {
        organization.addManager(actor2);
        boolean isFounder = organization.isFounder(actor2);
        assertFalse(isFounder);
    }

    @Test
    void whenIsFounder_givenNotManager_thenReturnFalse() {
        boolean isFounder = organization.isFounder(actor3);
        assertFalse(isFounder);
    }

    @Test
    void whenEditOrganization_givenValidData_thenNotThrowException() {
        String newName = "Magen David Kachol";
        String newDescription = "Magen David Kachol is Israel's national emergency water services organization.";
        String newPhoneNumber = "0548124081";
        String newEmail = "madk@gmail.com";

        assertDoesNotThrow(() -> organization.editOrganization(newName, newDescription, newPhoneNumber, newEmail));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"o", "oooooooooooooooooooooooooooooooooooooooooooooooooorganization"})
    void whenEditOrganization_givenInvalidName_thenThrowException(String newName) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.editOrganization(newName, description, phoneNumber, email);
        });
        String expectedError = String.format("Invalid organization name: %s.\n", newName);
        assertEquals(expectedError, exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"d", "ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddescription"})
    void whenEditOrganization_givenInvalidDescription_thenThrowException(String newDescription) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.editOrganization(name, newDescription, phoneNumber, email);
        });
        String expectedError = String.format("Invalid organization description: %s.\n", newDescription);
        assertEquals(expectedError, exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"+972-50-1234567", "050-123-4567", "+97250123456789", "+97250123456", "972501234567", "1234567890", "+9724-1234567", "+97250A234567", "050-12345@67", "05 0 123 456 7", "+123501234567", "01501234567", "501234567"})
    void whenEditOrganization_givenInvalidPhoneNumber_thenThrowException(String newPhoneNumber) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.editOrganization(name, description, newPhoneNumber, email);
        });
        String expectedError = String.format("Invalid phone number: %s.\n", newPhoneNumber);
        assertEquals(expectedError, exception.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"plainaddress", "@missingusername.com", "username@.com", "username@domain", "username@domain.c", "user@domain,com", "user@domain#com", "user@domain.com.", "user@domain@domain.com", "user@domain.-com", "user@domain_com.com", "user@domain@domain.com", "user@domain.com/another"})
    void whenEditOrganization_givenInvalidEmail_thenThrowException(String newEmail) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.editOrganization(name, description, phoneNumber, newEmail);
        });
        String expectedError = String.format("Invalid email: %s.", newEmail);
        assertEquals(expectedError, exception.getMessage());
    }

    @Test
    void givenNewManager_whenAddManager_thenAdd() {
        assertEquals(managers, organization.getManagerUsernames());

        organization.addManager(actor2);
        List<String> expectedAfterAddActor2 = List.of(actor1, actor2);
        assertEquals(expectedAfterAddActor2, organization.getManagerUsernames());

        organization.addManager(actor3);
        List<String> expectedAfterAddActor3 = List.of(actor1, actor2, actor3);
        assertEquals(expectedAfterAddActor3, organization.getManagerUsernames());
    }

    @Test
    void givenManager_whenRemoveManager_thenRemove() {
        organization.addManager(actor2);
        List<String> expectedAfterAddActor2 = List.of(actor1, actor2);
        assertEquals(expectedAfterAddActor2, organization.getManagerUsernames());

        assertDoesNotThrow(() -> organization.removeManager(actor2));
        assertEquals(managers, organization.getManagerUsernames());
    }

    @Test
    void givenNonManager_whenRemoveManager_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.removeManager(actor2);
        });
        assertEquals(OrganizationErrors.makeUserIsNotAManagerError(actor2, name), exception.getMessage());
    }

    @Test
    void givenFounder_whenRemoveManager_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.removeManager(actor1);
        });
        assertEquals(OrganizationErrors.makeFounderCanNotBeRemovedError(actor1, name), exception.getMessage());
    }

    @Test
    void givenManager_whenResign_thenNoException() {
        organization.addManager(actor2);
        List<String> expectedAfterAddActor2 = List.of(actor1, actor2);
        assertEquals(expectedAfterAddActor2, organization.getManagerUsernames());

        assertDoesNotThrow(() -> organization.resign(actor2));
        assertEquals(managers, organization.getManagerUsernames());
    }

    @Test
    void givenNotManager_whenResign_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.resign(actor2);
        });
        assertEquals(OrganizationErrors.makeUserIsNotAManagerError(actor2, name), exception.getMessage());
    }

    @Test
    void givenFounder_whenResign_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.resign(actor1);
        });
        assertEquals(OrganizationErrors.makeFounderCanNotResignError(actor1, name), exception.getMessage());
    }

    @Test
    void givenManager_whenSetFounder_thenSetFounder() {
        organization.addManager(actor2);
        assertEquals(actor1, organization.getFounderUsername());

        assertDoesNotThrow(() -> organization.setFounder(actor2));
        assertEquals(actor2, organization.getFounderUsername());
    }

    @Test
    void givenNotManager_whenSetFounder_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.setFounder(actor2);
        });
        assertEquals(OrganizationErrors.makeUserIsNotAManagerError(actor2, name), exception.getMessage());
    }

    @Test
    void givenNewVolunteering_whenAddVolunteering_thenAddVolunteering() {
        assertEquals(volunteerings, organization.getVolunteeringIds());

        assertDoesNotThrow(() -> organization.addVolunteering(1));
        assertEquals(List.of(0, 1), organization.getVolunteeringIds());
    }

    @Test
    void givenExistingVolunteering_whenAddVolunteering_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.addVolunteering(0);
        });
        assertEquals(volunteerings, organization.getVolunteeringIds());
        assertEquals(OrganizationErrors.makeVolunteeringAlreadyExistsError(0, name), exception.getMessage());
    }

    @Test
    void givenExistingVolunteering_whenRemoveVolunteering_thenRemoveVolunteering() {
        assertEquals(volunteerings, organization.getVolunteeringIds());

        assertDoesNotThrow(() -> organization.removeVolunteering(0));
        assertEquals(List.of(), organization.getVolunteeringIds());
    }

    @Test
    void givenNewVolunteering_whenRemoveVolunteering_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.removeVolunteering(1);
        });
        assertEquals(volunteerings, organization.getVolunteeringIds());
        assertEquals(OrganizationErrors.makeVolunteeringDoesNotExistsError(1, name), exception.getMessage());
    }

    @Test
    void givenFounder_whenUploadSignature_thenUpload() {
        assertNull(organization.getSignature());

        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);

        organization.uploadSignature(actor1, randomBytes);

        assertEquals(randomBytes, organization.getSignature());
    }

    @Test
    void givenNonFounder_whenUploadSignature_thenThrowException() {
        assertNull(organization.getSignature());

        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.uploadSignature(actor2, randomBytes);
        });
        assertEquals("Only organization founder can upload signature.", exception.getMessage());

        assertNull(organization.getSignature());
    }

    @Test
    void givenManager_whenGetSignature_thenReturnSignature() {
        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);

        organization.uploadSignature(actor1, randomBytes);
        organization.addManager(actor2);

        byte[] res1 = organization.getSignature(actor1);
        byte[] res2 = organization.getSignature(actor2);

        assertEquals(randomBytes, res1);
        assertEquals(randomBytes, res2);
    }

    @Test
    void givenNonManager_whenGetSignature_thenReturnSignature() {
        byte[] randomBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(randomBytes);

        organization.uploadSignature(actor1, randomBytes);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organization.getSignature(actor2);
        });
        assertEquals("Only organization manager can get signature.", exception.getMessage());
    }
}