package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private UsersFacade usersFacade;
    private AuthFacade authFacade;

    @Autowired
    public UserService(FacadeManager facadeManager){
        this.usersFacade = facadeManager.getUsersFacade();
        this.authFacade = facadeManager.getAuthFacade();
        //this.usersFacade.registerAdmin("admin","password","admin","admin@gmail.com","052-0520520", new Date());

/*
        this.usersFacade.register("TheDoctor", "DOOMDOOLOOM12345", "The", "doctor@tardis.com", "052-0520520", new Date());
        this.usersFacade.register("EyalShapiro", "1234EYAL1234", "Eyal", "eyald@post.bgu.ac.il", "052-0520520", new Date());
        this.usersFacade.register("DanaFriedman", "1234DANA1234", "Dana", "dafr@post.bgu.ac.il", "052-0520520", new Date());
        this.usersFacade.register("NirAharoni", "1234NIR1234", "Nir", "nirahar@post.bgu.ac.il", "052-0520520", new Date());
        this.usersFacade.register("GalPinto", "galpinto", "Gal", "pintogal@post.bgu.ac.il", "052-0520520", new Date());*/
    }

    private void checkToken(String token, String username){
        if(!authFacade.getNameFromToken(token).equals(username)){
            throw new IllegalArgumentException("Invalid token");
        }
        if(usersFacade.isBanned(username)) {
            throw new IllegalArgumentException("Banned user.");
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

    public Response<User> getUserByUsername(String username) {
        try{
            return Response.createResponse(usersFacade.getUser(username));
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

    public Response<List<HourApprovalRequest>> getApprovedHours(String token, String username) {
        try {
            checkToken(token, username);
            List<HourApprovalRequest> result = usersFacade.getApprovedHours(username);
            return Response.createResponse(result);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Map<String, Double>> leaderboard(String token, String username) {
        try {
            checkToken(token, username);
            Map<String, Double> result = usersFacade.leaderboard();
            return Response.createResponse(result);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> setLeaderboard(String token, String username, boolean leaderboard) {
        try {
            checkToken(token, username);
            usersFacade.setLeaderboard(username, leaderboard);
            return Response.createResponse(true);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> banUser(String token, String actor, String username) {
        try {
            checkToken(token, actor);
            usersFacade.banUser(username, actor);
            return Response.createResponse(true);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllUserEmails(String token, String actor) {
        try {
            checkToken(token, actor);
            List<String> res = usersFacade.getAllUserEmails();
            return Response.createResponse(res);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }
}
