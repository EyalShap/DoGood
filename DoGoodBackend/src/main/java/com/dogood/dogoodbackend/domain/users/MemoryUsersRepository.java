package com.dogood.dogoodbackend.domain.users;

import java.util.*;

public class MemoryUsersRepository implements UsersRepository {
    private Map<String, User> users;

    public MemoryUsersRepository() {
        this.users = new HashMap<>();
    }

    @Override
    public User createUser(String username, String email, String name, String password, String phoneNumber, Date birthDate) {
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("Register failed - username:" + username + " already exists");
        }
        User user = new User(username, email, name, password, phoneNumber, birthDate);
        users.put(username, user);
        return user;
    }

    @Override
    public void removeUser(String username) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("Remove user failed - username:" + username + " doesn't exist");
        }
    }

    @Override
    public void updateUserFields(String username, List<String> emails, String name, String password, String phoneNumber) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("Update failed - username:" + username + "doesn't exist");
        }
        User user = users.get(username);
        user.updateFields(emails, name, password, phoneNumber);
    }

    @Override
    public User getUser(String username) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("Username:" + username + "doesn't exist");
        }
        return users.get(username);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
