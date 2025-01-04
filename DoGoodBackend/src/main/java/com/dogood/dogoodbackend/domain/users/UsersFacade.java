package com.dogood.dogoodbackend.domain.users;

import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;

import java.util.Date;

public class UsersFacade {
    private UsersRepository repository;
    private AuthFacade authFacade;

    public UsersFacade(UsersRepository repository, AuthFacade authFacade) {
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
            User user = new User(username, email, name, password, phone, birthDate);
        }
    }

    public boolean isAdmin(String username) {
        User user = getUser(username);
        return user.isAdmin();
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
}