package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.api.organizationrequests.CreateOrganizationRequest;
import com.dogood.dogoodbackend.api.organizationrequests.CreateVolunteeringRequest;
import com.dogood.dogoodbackend.domain.organizations.OrganizationDTO;
import com.dogood.dogoodbackend.domain.organizations.Request;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.service.OrganizationService;
import com.dogood.dogoodbackend.service.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.dogood.dogoodbackend.utils.GetToken.getToken;

@RestController
@CrossOrigin
@RequestMapping("/api/organizations")
public class OrganizationAPI {
    @Autowired
    OrganizationService organizationService;

    @PostMapping("/createOrganization")
    public Response<Integer> createOrganization(@RequestBody CreateOrganizationRequest createOrganizationRequest, HttpServletRequest request) {
        String token = getToken(request);

        String name = createOrganizationRequest.getName();
        String description = createOrganizationRequest.getDescription();
        String phoneNumber = createOrganizationRequest.getPhoneNumber();
        String email = createOrganizationRequest.getEmail();
        String actor = createOrganizationRequest.getActor();
        return organizationService.createOrganization(token, name, description, phoneNumber, email, actor);
    }

    @DeleteMapping("/removeOrganization")
    public Response<Boolean> removeOrganization(@RequestParam int orgId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.removeOrganization(token, orgId, actor);
    }

    @PutMapping("/editOrganization")
    public Response<Boolean> editOrganization(@RequestParam int orgId, @RequestBody CreateOrganizationRequest createOrganizationRequest, HttpServletRequest request) {
        String token = getToken(request);

        String name = createOrganizationRequest.getName();
        String description = createOrganizationRequest.getDescription();
        String phoneNumber = createOrganizationRequest.getPhoneNumber();
        String email = createOrganizationRequest.getEmail();
        String actor = createOrganizationRequest.getActor();
        return organizationService.editOrganization(token, orgId, name, description, phoneNumber, email, actor);
    }

    @PostMapping("/createVolunteering")
    public Response<Integer> createVolunteering(@RequestBody CreateVolunteeringRequest createVolunteeringRequest, HttpServletRequest request) {
        String token = getToken(request);

        int orgId = createVolunteeringRequest.getOrganizationId();
        String volunteeringName = createVolunteeringRequest.getVolunteeringName();
        String description = createVolunteeringRequest.getVolunteeringDescription();
        String actor = createVolunteeringRequest.getActor();
        return organizationService.createVolunteering(token, orgId, volunteeringName, description, actor);
    }

    @DeleteMapping("/removeVolunteering")
    public Response<Boolean> removeVolunteering(@RequestParam int organizationId, @RequestParam int volunteeringId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.removeVolunteering(token, organizationId, volunteeringId, actor);
    }

    @PostMapping("/sendAssignManagerRequest")
    public Response<Boolean> sendAssignManagerRequest(@RequestBody GeneralRequest assignManagerRequest, @RequestParam String newManager, HttpServletRequest request) {
        String token = getToken(request);

        int orgId = assignManagerRequest.getId();
        String actor = assignManagerRequest.getActor();
        return organizationService.sendAssignManagerRequest(token, newManager, actor, orgId);
    }

    @PostMapping("/handleAssignManagerRequest")
    public Response<Boolean> handleAssignManagerRequest(@RequestBody GeneralRequest handleManagerRequest, @RequestParam boolean approved, HttpServletRequest request) {
        String token = getToken(request);

        int orgId = handleManagerRequest.getId();
        String actor = handleManagerRequest.getActor();
        return organizationService.handleAssignManagerRequest(token, actor, orgId, approved);
    }

    @DeleteMapping("/resign")
    public Response<Boolean> resign(@RequestParam int orgId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.resign(token, actor, orgId);
    }

    @DeleteMapping("/removeManager")
    public Response<Boolean> removeManager(@RequestParam int orgId, @RequestParam String actor, @RequestParam String managerToRemove, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.removeManager(token, actor, managerToRemove, orgId);
    }

    @PutMapping("/setFounder")
    public Response<Boolean> setFounder(@RequestBody GeneralRequest setFounderRequest, @RequestParam String newFounder, HttpServletRequest request) {
        String token = getToken(request);

        int orgId = setFounderRequest.getId();
        String actor = setFounderRequest.getActor();
        return organizationService.setFounder(token, actor, newFounder, orgId);
    }

    @GetMapping("/getUserRequests")
    public Response<List<Request>> getUserRequests(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.getUserRequests(token, actor);
    }

    @GetMapping("/getOrganization")
    public Response<OrganizationDTO> getOrganization(@RequestParam int orgId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.getOrganization(token, orgId, actor);
    }

    @GetMapping("/getAllOrganizations")
    public Response<List<OrganizationDTO>> getAllOrganizations(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.getAllOrganizations(token, actor);
    }

    @GetMapping("/isManager")
    public Response<Boolean> isManager(@RequestParam int orgId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.isManager(token, actor, orgId);
    }

    @GetMapping("/getOrganizationVolunteerings")
    public Response<List<VolunteeringDTO>> getOrganizationVolunteerings(@RequestParam int orgId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.getOrganizationVolunteerings(token, actor, orgId);
    }

    @GetMapping("/getOrganizationName")
    public Response<String> getOrganizationName(@RequestParam int orgId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.getOrganizationName(token, actor, orgId);
    }
}
