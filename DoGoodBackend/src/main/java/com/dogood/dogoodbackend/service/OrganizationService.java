package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.organizations.OrganizationDTO;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.requests.RequestObject;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.List;

@Service
@Transactional
public class OrganizationService {
    private OrganizationsFacade organizationsFacade;
    private AuthFacade authFacade;
    private UsersFacade usersFacade;

    @Autowired
    public OrganizationService(FacadeManager facadeManager){
        this.organizationsFacade = facadeManager.getOrganizationsFacade();
        this.authFacade = facadeManager.getAuthFacade();
        this.usersFacade = facadeManager.getUsersFacade();
    }

    private void checkToken(String token, String username){
        if(!authFacade.getNameFromToken(token).equals(username)){
            throw new IllegalArgumentException("Invalid token");
        }
        if (usersFacade.isBanned(username)) {
            throw new IllegalArgumentException("Banned user.");
        }
    }

    public Response<Integer> createOrganization(String token, String name, String description, String phoneNumber, String email, String actor) {
        try {
            checkToken(token, actor);
            int orgId = organizationsFacade.createOrganization(name, description, phoneNumber, email, actor);
            return Response.createResponse(orgId);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeOrganization(String token, int organizationId, String actor) {
        try {
            checkToken(token, actor);
            organizationsFacade.removeOrganization(organizationId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> editOrganization(String token, int organizationId, String name, String description, String phoneNumber, String email, String actor) {
        try {
            checkToken(token, actor);
            organizationsFacade.editOrganization(organizationId, name, description, phoneNumber, email, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> createVolunteering(String token, int organizationId, String volunteeringName, String volunteeringDescription, String actor) {
        try {
            checkToken(token, actor);
            int volunteeringId = organizationsFacade.createVolunteering(organizationId, volunteeringName, volunteeringDescription, actor);
            return Response.createResponse(volunteeringId);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeVolunteering(String token, int organizationId, int volunteeringId, String actor) {
        try {
            checkToken(token, actor);
            organizationsFacade.removeVolunteering(organizationId, volunteeringId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> sendAssignManagerRequest(String token, String newManager, String actor, int organizationId) {
        try {
            checkToken(token, actor);
            organizationsFacade.sendAssignManagerRequest(newManager, actor, organizationId);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> handleAssignManagerRequest(String token, String actor, int organizationId, boolean approved) {
        try {
            checkToken(token, actor);
            organizationsFacade.handleAssignManagerRequest(actor, organizationId, approved);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> resign(String token, String actor, int organizationId) {
        try {
            checkToken(token, actor);
            organizationsFacade.resign(actor, organizationId);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeManager(String token, String actor, String managerToRemove, int organizationId) {
        try {
            checkToken(token, actor);
            organizationsFacade.removeManager(actor, managerToRemove, organizationId);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> setFounder(String token, String actor, String newFounder, int organizationId) {
        try {
            checkToken(token, actor);
            organizationsFacade.setFounder(actor, newFounder, organizationId);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<Request>> getUserRequests(String token, String actor) {
        try {
            checkToken(token, actor);
            List<Request> requests = organizationsFacade.getUserRequests(actor);
            return Response.createResponse(requests);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<OrganizationDTO> getOrganization(String token, int organizationId, String actor) {
        try {
            checkToken(token, actor);
            OrganizationDTO org = organizationsFacade.getOrganization(organizationId);
            return Response.createResponse(org);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<OrganizationDTO>> getAllOrganizations(String token, String actor) {
        try {
            checkToken(token, actor);
            List<OrganizationDTO> orgs = organizationsFacade.getAllOrganizations();
            return Response.createResponse(orgs);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> isManager(String token, String username, int organizationId) {
        try {
            checkToken(token, username);
            boolean res = organizationsFacade.isManager(username, organizationId);
            return Response.createResponse(res);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringDTO>> getOrganizationVolunteerings(String token, String actor, int organizationId) {
        try {
            checkToken(token, actor);
            List<VolunteeringDTO> res = organizationsFacade.getOrganizationVolunteerings(organizationId);
            return Response.createResponse(res);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> getOrganizationName(String token, String actor, int organizationId) {
        try {
            checkToken(token, actor);
            String res = organizationsFacade.getOrganization(organizationId).getName();
            return Response.createResponse(res, null);
        }
        catch (Exception e) {
            return Response.createResponse(null, e.getMessage());
        }
    }

    public Response<List<Integer>> getUserVolunteeerings(String token, String actor, int organizationId) {
        try {
            checkToken(token, actor);
            List<Integer> res = organizationsFacade.getUserVolunteerings(organizationId, actor);
            return Response.createResponse(res, null);
        }
        catch (Exception e) {
            return Response.createResponse(null, e.getMessage());
        }
    }

    public Response<Boolean> changeImageInOrganization(String token, int organizationId, String image, boolean add, String actor) {
        try {
            checkToken(token, actor);
            organizationsFacade.changeImageInOrganization(organizationId, image, add, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(null, e.getMessage());
        }
    }

    public Response<Boolean> uploadSignature(String token, int organizationId, String actor, MultipartFile signature) {
        try {
            checkToken(token, actor);
            organizationsFacade.uploadSignature(organizationId, actor, signature);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(null, e.getMessage());
        }
    }

    public Response<byte[]> getSignature(String token, int organizationId, String actor) {
        try {
            checkToken(token, actor);
            byte[] res = organizationsFacade.getSignature(organizationId, actor);
            return Response.createResponse(res);
        }
        catch (Exception e) {
            return Response.createResponse(null, e.getMessage());
        }
    }
}
