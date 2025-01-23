package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.api.userrequests.LoginRequest;
import com.dogood.dogoodbackend.api.userrequests.RegisterRequest;
import com.dogood.dogoodbackend.api.userrequests.UpdateUserRequest;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.VolunteeringInHistory;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ApprovedHours;
import com.dogood.dogoodbackend.service.Response;
import com.dogood.dogoodbackend.service.UserService;
import com.dogood.dogoodbackend.service.VolunteeringService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.dogood.dogoodbackend.utils.GetToken.getToken;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserAPI {
    @Autowired
    UserService userService; //this is also singleton

    @PostMapping("/login")
    public Response<String> login(@RequestBody LoginRequest body) {
        return userService.login(body.getUsername(), body.getPassword());
    }

    @PostMapping("/logout")
    public Response<String> logout(HttpServletRequest request) {
        return userService.logout(getToken(request));
    }

    @PostMapping("/register")
    public Response<String> register(@RequestBody RegisterRequest body) {
        return userService.register(body.getUsername(), body.getPassword(), body.getName(), body.getEmail(), body.getPhone(), body.getBirthDate());
    }

    @GetMapping("/isAdmin")
    public Response<Boolean> isAdmin(@RequestParam String username) {
        return userService.isAdmin(username);
    }

    @GetMapping("/getUserByUsername")
    public Response<User> getUserByUsername(@RequestParam String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping("/getUserByToken")
    public Response<User> getUserByToken(HttpServletRequest request) {
        return userService.getUserByToken(getToken(request));
    }

    @PatchMapping("/updateUserFields")
    public Response<String> updateUserFields(@RequestParam String username, @RequestBody UpdateUserRequest body, HttpServletRequest request){
        String token = getToken(request);
        return userService.updateUserFields(token, username, body.getPassword(), body.getEmails(), body.getName(), body.getPhone());
    }

    @PatchMapping("/updateUserSkills")
    public Response<String> updateUserSkills(@RequestParam String username, @RequestBody List<String> body, HttpServletRequest request){
        String token = getToken(request);
        return userService.updateUserSkills(token, username, body);
    }

    @PatchMapping("/updateUserPreferences")
    public Response<String> updateUserPreferences(@RequestParam String username, @RequestBody List<String> body, HttpServletRequest request){
        String token = getToken(request);
        return userService.updateUserPreferences(token, username, body);
    }

    @GetMapping("/getUserApprovedHours")
    public Response<List<ApprovedHours>> getUserApprovedHours(@RequestParam String username, HttpServletRequest request) {
        String token = getToken(request);
        return userService.getApprovedHours(token, username);
    }
}
