package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.organizations.OrganizationDTO;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.organizations.Request;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {
    private OrganizationsFacade organizationsFacade;

    @Autowired
    public OrganizationService(FacadeManager facadeManager){
        this.organizationsFacade = facadeManager.getOrganizationsFacade();
    }

    public Response<Integer> createOrganization(String token, String name, String description, String phoneNumber, String email, String actor) {
        //TODO: check token

        try {
            int orgId = organizationsFacade.createOrganization(name, description, phoneNumber, email, actor);
            return Response.createResponse(orgId);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeOrganization(String token, int organizationId, String actor) {
        //TODO: check token

        try {
            organizationsFacade.removeOrganization(organizationId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> editOrganization(String token, int organizationId, String name, String description, String phoneNumber, String email, String actor) {
        //TODO: check token

        try {
            organizationsFacade.editOrganization(organizationId, name, description, phoneNumber, email, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> createVolunteering(String token, int organizationId, String volunteeringName, String volunteeringDescription, String actor) {
        //TODO: check token

        try {
            int volunteeringId = organizationsFacade.createVolunteering(organizationId, volunteeringName, volunteeringDescription, actor);
            return Response.createResponse(volunteeringId);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    /*public Response<Boolean> removeVolunteering(String token, int organizationId, int volunteeringId, String actor) {
        //TODO: check token

        try {
            organizationsFacade.removeVolunteering(organizationId, volunteeringId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }*/

    public Response<Boolean> sendAssignManagerRequest(String token, String newManager, String actor, int organizationId) {
        //TODO: check token

        try {
            organizationsFacade.sendAssignManagerRequest(newManager, actor, organizationId);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> handleAssignManagerRequest(String token, String actor, int organizationId, boolean approved) {
        //TODO: check token

        try {
            organizationsFacade.handleAssignManagerRequest(actor, organizationId, approved);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> resign(String token, String actor, int organizationId) {
        //TODO: check token

        try {
            organizationsFacade.resign(actor, organizationId);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeManager(String token, String actor, String managerToRemove, int organizationId) {
        //TODO: check token

        try {
            organizationsFacade.removeManager(actor, managerToRemove, organizationId);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> setFounder(String token, String actor, String newFounder, int organizationId) {
        //TODO: check token

        try {
            organizationsFacade.setFounder(actor, newFounder, organizationId);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<Request>> getUserRequests(String token, String actor) {
        //TODO: check token

        try {
            List<Request> requests = organizationsFacade.getUserRequests(actor);
            return Response.createResponse(requests);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<OrganizationDTO> getOrganization(String token, int organizationId, String actor) {
        //TODO: check token

        try {
            OrganizationDTO org = organizationsFacade.getOrganization(organizationId);
            return Response.createResponse(org);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<OrganizationDTO>> getAllOrganizations(String token, String actor) {
        //TODO: check token

        try {
            List<OrganizationDTO> orgs = organizationsFacade.getAllOrganizations();
            return Response.createResponse(orgs);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> isManager(String token, String username, int organizationId) {
        //TODO: check token

        try {
            boolean res = organizationsFacade.isManager(username, organizationId);
            return Response.createResponse(res);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringDTO>> getOrganizationVolunteerings(String token, String actor, int organizationId) {
        //TODO: check token

        try {
            List<VolunteeringDTO> res = organizationsFacade.getOrganizationVolunteerings(organizationId);
            return Response.createResponse(res);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> getOrganizationName(String token, String actor, int organizationId) {
        //TODO: check token

        try {
            String res = organizationsFacade.getOrganization(organizationId).getName();
            return Response.createResponse(res, null);
        }
        catch (Exception e) {
            return Response.createResponse(null, e.getMessage());
        }
    }

    public Response<List<Integer>> getUserVolunteeerings(String token, String actor, int organizationId) {
        //TODO: check token

        try {
            List<Integer> res = organizationsFacade.getUserVolunteerings(organizationId, actor);
            return Response.createResponse(res, null);
        }
        catch (Exception e) {
            return Response.createResponse(null, e.getMessage());
        }
    }
}
