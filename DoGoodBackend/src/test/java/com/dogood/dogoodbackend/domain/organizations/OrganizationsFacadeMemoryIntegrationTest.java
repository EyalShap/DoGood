package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.requests.RequestObject;
import com.dogood.dogoodbackend.domain.requests.RequestRepository;
import com.dogood.dogoodbackend.domain.users.MemoryUserRepository;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.volunteerings.MemoryVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.MemorySchedulingManager;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class OrganizationsFacadeMemoryIntegrationTest {
    private OrganizationsFacade organizationsFacade;
    private int organizationId;
    private final String name1 = "Magen David Adom";
    private final String description1 = "Magen David Adom is Israel's national emergency medical and blood services organization.";
    private final String phoneNumber1 = "0548124087";
    private final String email1 = "mada@gmail.com";
    private final String name2 = "Magen David Kachol";
    private final String description2 = "Water services";
    private final String phoneNumber2 = "0547424087";
    private final String email2 = "madk@gmail.com";
    private OrganizationDTO organization1, organization2;
    private final String actor1 = "TheDoctor";
    private final String actor2 = "NotTheDoctor";
    private RequestRepository requestRepository;
    private VolunteeringFacade volunteeringFacade;
    private OrganizationRepository organizationRepository;

    @BeforeEach
    void setUp() {
        //this.requestRepository = new MemoryRequestRepository();
        this.requestRepository = null;
        this.organizationRepository = new MemoryOrganizationRepository();
        //this.organizationsFacade = new OrganizationsFacade(new UsersFacade(new MemoryUserRepository(), new AuthFacade()), organizationRepository, requestRepository);
        this.organizationId = this.organizationsFacade.createOrganization(name1, description1, phoneNumber1, email1, actor1);
       // this.volunteeringFacade = new VolunteeringFacade(new UsersFacade(new MemoryUserRepository(), new AuthFacade()), organizationsFacade, new MemoryVolunteeringRepository(), new MemorySchedulingManager(), null);
        this.organizationsFacade.setVolunteeringFacade(volunteeringFacade);

        this.organization1 = new OrganizationDTO(new Organization(organizationId, name1, description1, phoneNumber1, email1, actor1));
        this.organization2 = new OrganizationDTO(new Organization(0, name2, description2, phoneNumber2, email2, actor1));
    }

    @Test
    void givenValidFields_whenCreateOrganization_thenCreate() {
        int newOrgId = organizationsFacade.createOrganization(name2, description2, phoneNumber2, email2, actor1);

        assertEquals(2, organizationsFacade.getAllOrganizations().size());

        organization2.setId(newOrgId);
        assertEquals(organization2, organizationsFacade.getOrganization(newOrgId));
    }

    @Test
    void givenInvalidFields_whenCreateOrganization_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.createOrganization("", "", "", "", actor1);
        });
        StringBuilder expectedError = new StringBuilder();
        expectedError
                .append("Invalid organization name: .\n")
                .append("Invalid organization description: .\n")
                .append("Invalid phone number: .\n")
                .append("Invalid email: .");

        assertEquals(expectedError.toString(), exception.getMessage());
    }

    //TODO when user is implemented
    /*@Test
    void givenExistingIdAdminActor_whenRemoveOrganization_thenRemove() {
        organizationsFacade.removeOrganization(organizationId, actor2);
        assertEquals(0, organizationsFacade.getAllOrganizations().size());
    }*/

    @Test
    void givenExistingIdAndFounder_whenRemoveOrganization_thenRemove() {
        assertEquals(1, organizationsFacade.getAllOrganizations().size());
        organizationsFacade.removeOrganization(organizationId, actor1);
        assertEquals(0, organizationsFacade.getAllOrganizations().size());
    }

    @Test
    void givenExistingIdAndManager_whenRemoveOrganization_thenThrowException() {
        assertEquals(1, organizationsFacade.getAllOrganizations().size());

        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeOrganization(organizationId, actor2);
        });

        assertEquals(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor2, name1, "remove the organization"), exception.getMessage());
        assertEquals(1, organizationsFacade.getAllOrganizations().size());
    }

    @Test
    void givenNonExistingId_whenRemoveOrganization_thenThrowException() {
        assertEquals(1, organizationsFacade.getAllOrganizations().size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeOrganization(organizationId + 1, actor1);
        });

        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
        assertEquals(1, organizationsFacade.getAllOrganizations().size());
    }

    // -----------------------------

    //TODO when user is implemented
    /*@Test
    void givenExistingIdAdminActor_whenEdit_thenRemove() {
        organizationsFacade.removeOrganization(organizationId, actor2);
        assertEquals(0, organizationsFacade.getAllOrganizations().size());
    }*/

    @Test
    void givenExistingIdAndFounderAndValidFields_whenEditOrganization_thenEdit() {
        organization2.setId(organizationId);

        assertEquals(organization1, organizationsFacade.getOrganization(organizationId));

        organizationsFacade.editOrganization(organizationId, name2, description2, phoneNumber2, email2, actor1);

        assertEquals(organization2, organizationsFacade.getOrganization(organizationId));
    }

    @Test
    void givenExistingIdAndManagerAndValidFields_whenEditOrganization_thenEdit() {
        organization2.setId(organizationId);
        organization2.setManagerUsernames(List.of(actor1, actor2));

        assertEquals(organization1, organizationsFacade.getOrganization(organizationId));

        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);
        organizationsFacade.editOrganization(organizationId, name2, description2, phoneNumber2, email2, actor2);

        assertEquals(organization2, organizationsFacade.getOrganization(organizationId));
    }

    @Test
    void givenNonManager_whenEditOrganization_thenThrowException() {
        assertEquals(organization1, organizationsFacade.getOrganization(organizationId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.editOrganization(organizationId, name2, description2, phoneNumber2, email2, actor2);
        });

        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor2, name1, "edit the organization's details"), exception.getMessage());
        assertEquals(organization1, organizationsFacade.getOrganization(organizationId));
    }

    @Test
    void givenNonExistingId_whenEditOrganization_thenThrowException() {
        assertEquals(organization1, organizationsFacade.getOrganization(organizationId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.editOrganization(organizationId + 1, name2, description2, phoneNumber2, email2, actor2);
        });

        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
        assertEquals(organization1, organizationsFacade.getOrganization(organizationId));
    }

    @Test
    void givenInvalidFields_whenEditOrganization_thenThrowException() {
        assertEquals(organization1, organizationsFacade.getOrganization(organizationId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.editOrganization(organizationId, "", "", "", "", actor1);
        });

        StringBuilder expectedError = new StringBuilder();
        expectedError
                .append("Invalid organization name: .\n")
                .append("Invalid organization description: .\n")
                .append("Invalid phone number: .\n")
                .append("Invalid email: .");
        assertEquals(expectedError.toString(), exception.getMessage());
        assertEquals(organization1, organizationsFacade.getOrganization(organizationId));
    }

    @Test
    void givenManager_whenCreateVolunteering_thenCreate() {
        int volunteeringId = organizationsFacade.createVolunteering(organizationId, "Volunteering", "Description", actor1);
        VolunteeringDTO expected = new VolunteeringDTO(volunteeringId, organizationId, "Volunteering", "Description", null, null, new ArrayList<>());
        List<VolunteeringDTO> res = organizationsFacade.getOrganizationVolunteerings(organizationId);
        assertTrue(equalLists(List.of(expected), res));
    }

    @Test
    void givenNonManager_whenCreateVolunteering_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.createVolunteering(organizationId, "Volunteering", "Description", actor2);
        });

        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor2, name1, "create a new volunteering"), exception.getMessage());
    }

    @Test
    void givenNonExistingOrganization_whenCreateVolunteering_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.createVolunteering(organizationId + 1, "Volunteering", "Description", actor2);
        });

        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenManagerAndExistingVolunteering_whenRemoveVolunteering_thenRemoveVolunteering() {
        int volunteeringId = organizationsFacade.createVolunteering(organizationId, "Volunteering", "Description", actor1);

        assertEquals(List.of(volunteeringId), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());
        organizationsFacade.removeVolunteering(organizationId, volunteeringId, actor1);
        assertEquals(new ArrayList<>(), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());
    }

    @Test
    void givenNonManager_whenRemoveVolunteering_thenThrowException() {
        int volunteeringId = organizationsFacade.createVolunteering(organizationId, "Volunteering", "Description", actor1);

        assertEquals(List.of(volunteeringId), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeVolunteering(organizationId, volunteeringId, actor2);
        });
        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor2, name1, "remove a volunteering"), exception.getMessage());
        assertEquals(List.of(volunteeringId), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());
    }

    @Test
    void givenNonExistingVolunteering_whenRemoveVolunteering_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeVolunteering(organizationId, 0, actor1);
        });
        assertEquals(OrganizationErrors.makeVolunteeringDoesNotExistsError(0, name1), exception.getMessage());
    }

    @Test
    void givenNonExistingOrganization_whenRemoveVolunteering_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeVolunteering(organizationId + 1, 0, actor2);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenVolunteeringInAnotherOrganization_whenRemoveVolunteering_thenThrowException() {
        int orgId2 = organizationsFacade.createOrganization(name2, description2, phoneNumber2, email2, actor2);
        int volunteeringId = organizationsFacade.createVolunteering(orgId2, "Volunteering", "Description", actor2);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeVolunteering(organizationId, volunteeringId, actor1);
        });
        assertEquals(OrganizationErrors.makeVolunteeringDoesNotExistsError(volunteeringId, name1), exception.getMessage());
    }

    @Test
    void givenManagerAssigningNonManager_whenSendAssignManagerRequest_thenSendRequest() {
        assertEquals(0, requestRepository.getUserRequests(actor2, RequestObject.ORGANIZATION).size());
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        assertEquals(1, requestRepository.getUserRequests(actor2, RequestObject.ORGANIZATION).size());
    }

    @Test
    void givenNonManagerPreformingAction_whenSendAssignManagerRequest_thenThrowException() {
        assertEquals(0, requestRepository.getUserRequests("New Manager", RequestObject.ORGANIZATION).size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.sendAssignManagerRequest("New Manager", actor2, organizationId);
        });

        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor2, name1, "send assign manager request"), exception.getMessage());
        assertEquals(0, requestRepository.getUserRequests("New Manager", RequestObject.ORGANIZATION).size());
    }

    @Test
    void givenAssigningVolunteer_whenSendAssignManagerRequest_thenThrowException() {
        int volunteeringId = organizationsFacade.createVolunteering(organizationId, "Name", "Description", actor1);
        volunteeringFacade.requestToJoinVolunteering(actor2, volunteeringId, "Please");
        volunteeringFacade.acceptUserJoinRequest(actor1, volunteeringId, actor2, 0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        });

        assertEquals(OrganizationErrors.makeUserIsVolunteerInTheOrganizationError(actor2, name1), exception.getMessage());
    }

    @Test
    void givenManagerAssigned_whenSendAssignManagerRequest_thenThrowException() {
        assertEquals(0, requestRepository.getUserRequests(actor1, RequestObject.ORGANIZATION).size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.sendAssignManagerRequest(actor1, actor1, organizationId);
        });

        assertEquals(OrganizationErrors.makeUserIsAlreadyAManagerError(actor1, name1), exception.getMessage());
        assertEquals(0, requestRepository.getUserRequests(actor1, RequestObject.ORGANIZATION).size());
    }

    @Test
    void givenDoubleRequest_whenSendAssignManagerRequest_thenSendRequest() {
        assertEquals(0, requestRepository.getUserRequests(actor2, RequestObject.ORGANIZATION).size());
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        assertEquals(1, requestRepository.getUserRequests(actor2, RequestObject.ORGANIZATION).size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        });

        assertEquals(OrganizationErrors.makeAssignManagerRequestAlreadyExistsError(actor2, organizationId), exception.getMessage());
        assertEquals(1, requestRepository.getUserRequests(actor2, RequestObject.ORGANIZATION).size());
    }

    @Test
    void givenAcceptingRequest_whenHandleAssignManagerRequest_thenAddManager() {
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        assertEquals(1, requestRepository.getUserRequests(actor2, RequestObject.ORGANIZATION).size());

        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);
        assertEquals(0, requestRepository.getUserRequests(actor2, RequestObject.ORGANIZATION).size());
        assertTrue(organizationsFacade.isManager(actor2, organizationId));
    }

    @Test
    void givenDenyingRequest_whenHandleAssignManagerRequest_thenDoNothing() {
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        assertEquals(1, requestRepository.getUserRequests(actor2, RequestObject.ORGANIZATION).size());

        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, false);
        assertEquals(0, requestRepository.getUserRequests(actor2, RequestObject.ORGANIZATION).size());
        assertFalse(organizationsFacade.isManager(actor2, organizationId));
    }

    @Test
    void givenNoRequest_whenHandleAssignManagerRequest_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);
        });
        assertEquals(OrganizationErrors.makeAssignManagerRequestDoesNotExistError(actor2, organizationId), exception.getMessage());
    }

    @Test
    void givenManager_whenResign_thanRemoveManager() {
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);

        assertTrue(organizationsFacade.isManager(actor2, organizationId));

        organizationsFacade.resign(actor2, organizationId);

        assertFalse(organizationsFacade.isManager(actor2, organizationId));
    }

    @Test
    void givenFounder_whenResign_thanThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.resign(actor1, organizationId);
        });
        assertEquals(OrganizationErrors.makeFounderCanNotResignError(actor1, name1), exception.getMessage());
        assertTrue(organizationsFacade.isManager(actor1, organizationId));
    }

    @Test
    void givenNonManager_whenResign_thanThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.resign(actor2, organizationId);
        });
        assertEquals(OrganizationErrors.makeUserIsNotAManagerError(actor2, name1), exception.getMessage());
    }

    @Test
    void givenNonExistingOrganization_whenResign_thanThrowException() {
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.resign(actor2, organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenFounderRemovingManager_whenRemoveManager_thenRemove() {
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);

        assertTrue(organizationsFacade.isManager(actor2, organizationId));

        organizationsFacade.removeManager(actor1, actor2, organizationId);

        assertFalse(organizationsFacade.isManager(actor2, organizationId));
    }

    @Test
    void givenManagerNonFounderPreformingAction_whenRemoveManager_thenThrowException() {
        String anotherManager = "NotNotTheDoctor";
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);
        organizationsFacade.sendAssignManagerRequest(anotherManager, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(anotherManager, organizationId, true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeManager(actor2, anotherManager, organizationId);
        });
        assertEquals(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor2, name1, "remove a manager from organization"), exception.getMessage());
    }

    @Test
    void givenRemovingFounder_whenRemoveManager_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeManager(actor1, actor1, organizationId);
        });
        assertEquals(OrganizationErrors.makeFounderCanNotBeRemovedError(actor1, name1), exception.getMessage());
    }

    @Test
    void givenRemovingNonManager_whenRemoveManager_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeManager(actor1, actor2, organizationId);
        });
        assertEquals(OrganizationErrors.makeUserIsNotAManagerError(actor2, name1), exception.getMessage());
    }

    @Test
    void givenNonExistingOrganization_whenRemoveManager_thenThrowException() {
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeManager(actor1, actor2, organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenFounderSettingManager_whenSetFounder_thenSet() {
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);

        OrganizationDTO organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(actor1, organization.getFounderUsername());
        organizationsFacade.setFounder(actor1, actor2, organizationId);

        organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(actor2, organization.getFounderUsername());
    }

    @Test
    void givenFounderSettingNonManager_whenSetFounder_thenThrowException() {
        OrganizationDTO organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(actor1, organization.getFounderUsername());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.setFounder(actor1, actor2, organizationId);
        });
        assertEquals(OrganizationErrors.makeUserIsNotAManagerError(actor2, name1), exception.getMessage());

        organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(actor1, organization.getFounderUsername());
    }

    @Test
    void givenNonFounderSettingManager_whenSetFounder_thenThrowException() {
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);

        OrganizationDTO organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(actor1, organization.getFounderUsername());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.setFounder("NotNotTheDoctor", actor2, organizationId);
        });
        assertEquals(OrganizationErrors.makeNonFounderCanNotPreformActionError("NotNotTheDoctor", name1, "set a new founder to the organization"), exception.getMessage());

        organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(actor1, organization.getFounderUsername());
    }

    @Test
    void givenNonExistingOrganization_whenSetFounder_thenThrowException() {
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.setFounder(actor1, actor2, organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenFounderOfAnotherOrganization_whenSetFounder_thenThrowException() {
        organizationsFacade.createOrganization(name2, description2, phoneNumber2, email2, actor2);
        String anotherManager = "NotNotTheDoctor";
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);
        organizationsFacade.sendAssignManagerRequest(anotherManager, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(anotherManager, organizationId, true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.setFounder(actor2, anotherManager, organizationId);
        });
        assertEquals(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor2, name1, "set a new founder to the organization"), exception.getMessage());
    }

    @Test
    void givenNoRequests_getUserRequests_thenReturnEmptyList() {
        assertEquals(new ArrayList<>(), organizationsFacade.getUserRequests(actor2));
    }

    @Test
    void givenApprovedRequest_getUserRequests_thenReturnEmptyList() {
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        organizationsFacade.handleAssignManagerRequest(actor2, organizationId, true);
        assertEquals(new ArrayList<>(), organizationsFacade.getUserRequests(actor2));
    }

    @Test
    void givenPendingRequest_getUserRequests_thenReturnList() {
        List<Request> expected = List.of(new Request(actor2, actor1, organizationId, RequestObject.ORGANIZATION));
        organizationsFacade.sendAssignManagerRequest(actor2, actor1, organizationId);
        assertEquals(expected, organizationsFacade.getUserRequests(actor2));
    }

    @Test
    void givenExistingOrganization_whenGetOrganization_thenReturnOrganization() {
        assertEquals(organization1, organizationsFacade.getOrganization(organizationId));
    }

    @Test
    void givenNonExistingOrganization_whenGetOrganization_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.getOrganization(organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void getAllOrganizations() {
        assertEquals(List.of(organization1), organizationsFacade.getAllOrganizations());
    }

    @Test
    void givenManager_whenIsManager_thenReturnTrue() {
        assertTrue(organizationsFacade.isManager(actor1, organizationId));
    }

    @Test
    void givenNonManager_whenIsManager_thenReturnFalse() {
        assertFalse(organizationsFacade.isManager(actor2, organizationId));
    }

    @Test
    void givenNonExistingOrganization_whenIsManager_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.isManager(actor1, organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenNoVolunteerings_whenGetOrganizationVolunteerings_thenReturnEmptyList() {
        assertEquals(new ArrayList<>(), organizationsFacade.getOrganizationVolunteerings(organizationId));
    }

    @Test
    void givenVolunteerings_whenGetOrganizationVolunteerings_thenReturnList() {
        int volunteeringId = organizationsFacade.createVolunteering(organizationId, "Volunteering", "Description", actor1);
        List<Integer> res = organizationsFacade.getOrganization(organizationId).getVolunteeringIds();
        assertEquals(List.of(volunteeringId), res);
    }

    @Test
    void givenNonExistingOrganization_whenGetOrganizationVolunteerings_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.getOrganizationVolunteerings(organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    //TODO: change when equals in VolunteeringDTO is implemented
    private boolean equalLists(List<VolunteeringDTO> l1, List<VolunteeringDTO> l2) {
        if(l1.size() != l2.size()) {
            return false;
        }
        for(int i = 0; i < l1.size(); i++) {
            VolunteeringDTO volunteeringDTO1 = l1.get(i);
            VolunteeringDTO volunteeringDTO2 = l2.get(i);

            if(!(volunteeringDTO1.getId() == volunteeringDTO2.getId()
                    && volunteeringDTO1.getOrgId() == volunteeringDTO2.getOrgId()
                    && volunteeringDTO1.getName().equals(volunteeringDTO2.getName())
                    && volunteeringDTO1.getDescription().equals(volunteeringDTO2.getDescription())
                    && (volunteeringDTO1.getSkills() == null ? volunteeringDTO2.getSkills() == null : volunteeringDTO1.getSkills().equals(volunteeringDTO2.getSkills()))
                    && (volunteeringDTO1.getSkills() == null ? volunteeringDTO2.getCategories() == null : volunteeringDTO1.getCategories().equals(volunteeringDTO2.getCategories()))
                    && (volunteeringDTO1.getImagePaths() == null ? volunteeringDTO2.getImagePaths() == null : volunteeringDTO1.getImagePaths().equals(volunteeringDTO2.getImagePaths())))) {
                return false;
            }
        }
        return true;
    }

}