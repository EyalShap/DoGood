package com.dogood.dogoodbackend.domain.users;

import com.dogood.dogoodbackend.jparepos.UserJPA;

import java.util.Date;
import java.util.List;

public class DatabaseUserRepository implements UserRepository {
    private UserJPA jpa;

    public DatabaseUserRepository(UserJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public User createUser(String username, String email, String name, String password, String phoneNumber, Date birthDate) {
        if (jpa.findById(username).orElse(null) != null) {
            throw new IllegalArgumentException("Register failed - username:" + username + " already exists");
        }
        User user = new User(username, email, name, password, phoneNumber, birthDate);
        jpa.save(user);
        return user;
    }

    @Override
    public void removeUser(String username) {
        if (jpa.findById(username).orElse(null) == null) {
            throw new IllegalArgumentException("Remove user failed - username:" + username + " doesn't exist");
        }
        jpa.deleteById(username);
    }

    @Override
    public void updateUserFields(String username, List<String> emails, String name, String password, String phoneNumber) {
        User user = jpa.findById(username).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Update failed - username:" + username + "doesn't exist");
        }
        user.updateFields(emails, name, password, phoneNumber);
        jpa.save(user);
    }

    @Override
    public void updateUserFields(String username, List<String> emails, String name, String phoneNumber) {
        User user = jpa.findById(username).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Update failed - username:" + username + "doesn't exist");
        }
        user.updateFields(emails, name, phoneNumber);
        jpa.save(user);
    }

    @Override
    public User getUser(String username) {
        User user = jpa.findById(username).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Username:" + username + "doesn't exist");
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return jpa.findAll();
    }

    @Override
    public void saveUser(User user) {
        jpa.save(user);
    }
}
