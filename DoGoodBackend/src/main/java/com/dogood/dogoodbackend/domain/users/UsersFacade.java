package com.dogood.dogoodbackend.domain.users;

import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;

import java.util.Date;
import java.util.List;

public class UsersFacade {
    private UserRepository repository;
    private AuthFacade authFacade;

    public UsersFacade(UserRepository repository, AuthFacade authFacade) {
        this.repository = repository;
        this.authFacade = authFacade;
    }

    public String login(String username, String password) {
        User user = getUser(username);
        boolean correctPassword = user.checkPassword(password);
        if (!correctPassword) {
            throw new IllegalArgumentException("Invalid password given for user " + username);
        }
        String accessToken = authFacade.generateToken(username);
        return accessToken;
    }

    public void logout(String token) {
        String username = authFacade.getNameFromToken(token); // Throws an exception if the given access token doesn't exist (user isn't logged in).
        authFacade.invalidateToken(token); // Invalidates the token if it isn't invalidated (isn't logged out already), otherwise (somehow) throws an exception.
    }

    public void register(String username, String password, String name, String email, String phone, Date birthDate) {
        try {
            User user = getUser(username);
            // if user with the same username exists, cannot register it again
            throw new IllegalArgumentException("Register failed - username " + username + " already exists.");
        } catch (IllegalArgumentException e) {
            repository.createUser(username, email, name, password, phone, birthDate);
        }
    }

    public boolean isAdmin(String username) {
        User user = getUser(username);
        return user.isAdmin();
    }

    public void registerAdmin(String username, String password, String name, String email, String phone, Date birthDate) {
        try {
            User user = getUser(username);
            // if user with the same username exists, cannot register it again
            throw new IllegalArgumentException("Register failed - username " + username + " already exists.");
        } catch (IllegalArgumentException e) {
            repository.createUser(username, email, name, password, phone, birthDate);
            repository.setAdmin(username, true);
        }
    }


    private User getUser(String username) {
        return repository.getUser(username);
    }

    public String getUserIdByToken(String token) {
        return authFacade.getNameFromToken(token);
    }

    public User getUserByToken(String token) {
        return getUser(authFacade.getNameFromToken(token));
    }

    public boolean userExists(String username) {
        try {
            User user = getUser(username);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void updateUserFields(String username, String password, List<String> emails, String name, String phone){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        if(password == null){
            repository.updateUserFields(
                    username == null ? user.getUsername() : username,
                    emails == null ? user.getEmails() : emails,
                    name == null ? user.getName() : name,
                    phone == null ? user.getPhone() : phone);
        }
        else{
            repository.updateUserFields(
                    username == null ? user.getUsername() : username,
                    emails == null ? user.getEmails() : emails,
                    name == null ? user.getName() : name,
                    password,
                    phone == null ? user.getPhone() : phone);
        }
    }

    public void updateUserSkills(String username, List<String> skills){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.updateSkills(skills);
        repository.saveUser(user);
    }

    public void updateUserPreferences(String username, List<String> categories){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.updatePreferences(categories);
        repository.saveUser(user);
    }

    public void addUserVolunteering(String username, int volunteeringId){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.addVolunteering(volunteeringId);
        repository.saveUser(user);
    }

    public void addUserOrganization(String username, int organizationId){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.addOrganization(organizationId);
        repository.saveUser(user);
    }

    public void removeUserVolunteering(String username, int volunteeringId){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.removeVolunteering(volunteeringId);
        repository.saveUser(user);
    }

    public void addUserVolunteeringHistory(String username, VolunteeringDTO volunteeringDTO){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        user.addVolunteeringToHistory(volunteeringDTO);
        repository.saveUser(user);
    }

    public List<String> getUserSkills(String username){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        return user.getSkills();
    }

    public List<String> getUserPreferences(String username){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        return user.getPreferredCategories();
    }

    public List<VolunteeringDTO> getUserVolunteeringHistory(String username){
        if(!userExists(username)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(username);
        return user.getVolunteeringsInHistory();
    }

    public void removeUserOrganization(String actor, int organizationId) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User not found");
        }
        User user = getUser(actor);
        user.removeOrganization(organizationId);
        repository.saveUser(user);
    }
}