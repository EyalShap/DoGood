package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.api.organizationrequests.CreateOrganizationRequest;
import com.dogood.dogoodbackend.api.organizationrequests.CreateVolunteeringRequest;
import com.dogood.dogoodbackend.domain.organizations.OrganizationDTO;
import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.service.OrganizationService;
import com.dogood.dogoodbackend.service.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
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

    /*@DeleteMapping("/removeVolunteering")
    public Response<Boolean> removeVolunteering(@RequestParam int organizationId, @RequestParam int volunteeringId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.removeVolunteering(token, organizationId, volunteeringId, actor);
    }*/

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

    @GetMapping("/getUserAssignManagerRequests")
    public Response<List<Request>> getUserAssignManagerRequests(@RequestParam String actor, HttpServletRequest request) {
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

    @GetMapping("/getUserVolunteerings")
    public Response<List<Integer>> getUserVolunteerings(@RequestParam int organizationId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.getUserVolunteeerings(token, actor, organizationId);
    }

    @PostMapping("/addImageToOrganization")
    public Response<Boolean> addImageToOrganization(@RequestParam int organizationId, @RequestBody String image, @RequestParam String actor,  HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.changeImageInOrganization(token, organizationId, image, true, actor);
    }

    @DeleteMapping("/removeImageFromOrganization")
    public Response<Boolean> removeImageFromOrganization(@RequestParam int organizationId, @RequestParam String image, @RequestParam String actor,  HttpServletRequest request) {
        String token = getToken(request);

        return organizationService.changeImageInOrganization(token, organizationId, image, false, actor);
    }

    @PutMapping("/uploadSignature")
    public Response<Boolean> uploadSignature(@RequestParam int organizationId, @RequestParam String username, @RequestParam MultipartFile signature, HttpServletRequest request) {
        String token = getToken(request);
        return organizationService.uploadSignature(token, organizationId, username, signature);
    }

    @PutMapping("/removeSignature")
    public Response<Boolean> removeSignature(@RequestParam int organizationId, @RequestParam String username, HttpServletRequest request) {
        String token = getToken(request);
        return organizationService.uploadSignature(token, organizationId, username, null);
    }

    @GetMapping("/getSignature")
    public ResponseEntity<byte[]> getSignature(@RequestParam int organizationId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);
        Response<byte[]> res = organizationService.getSignature(token, organizationId, actor);

        if (res.getError()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res.getErrorString().getBytes());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/image")
                .body(res.getData());
    }

}
