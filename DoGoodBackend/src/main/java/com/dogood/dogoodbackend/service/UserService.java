package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.volunteerings.PastExperience;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private UsersFacade usersFacade;
    private AuthFacade authFacade;

    @Autowired
    public UserService(FacadeManager facadeManager){
        this.usersFacade = facadeManager.getUsersFacade();
        this.authFacade = facadeManager.getAuthFacade();


        this.usersFacade.register("TheDoctor", "DOOMDOOLOOM12345", "The", "doctor@tardis.com", "052-0520520", new Date());
        this.usersFacade.register("EyalShapiro", "1234EYAL1234", "Eyal", "eyald@post.bgu.ac.il", "052-0520520", new Date());
        this.usersFacade.register("DanaFriedman", "1234DANA1234", "Dana", "dafr@post.bgu.ac.il", "052-0520520", new Date());
        this.usersFacade.register("NirAharoni", "1234NIR1234", "Nir", "nirahar@post.bgu.ac.il", "052-0520520", new Date());
        this.usersFacade.register("GalPinto", "1234GAL1234", "Gal", "pintogal@post.bgu.ac.il", "052-0520520", new Date());
    }

    private void checkToken(String token, String username){
        if(!authFacade.getNameFromToken(token).equals(username)){
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public Response<String> login(String username, String password) {
        try{
            return Response.createResponse(usersFacade.login(username, password), null);
        }catch (Exception e){
            return Response.createResponse(null, e.getMessage());
        }
    }

    public Response<String> logout(String token) {
        try{
            usersFacade.logout(token);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> register(String username, String password, String name, String email, String phone, Date birthDate) {
        try{
            usersFacade.register(username, password, name, email, phone, birthDate);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> isAdmin(String username) {
        try{
            return Response.createResponse(usersFacade.isAdmin(username));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<User> getUserByToken(String token) {
        try{
            return Response.createResponse(usersFacade.getUserByToken(token));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateUserFields(String token, String username, String password, List<String> emails, String name, String phone){
        try{
            checkToken(token, username);
            usersFacade.updateUserFields(username, password, emails, name, phone);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateUserSkills(String token, String username, List<String> skills){
        try{
            checkToken(token, username);
            usersFacade.updateUserSkills(username, skills);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateUserPreferences(String token, String username, List<String> categories){
        try{
            checkToken(token, username);
            usersFacade.updateUserPreferences(username, categories);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }
}
