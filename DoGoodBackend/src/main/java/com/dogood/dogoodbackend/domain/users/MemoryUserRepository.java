package com.dogood.dogoodbackend.domain.users;

import org.springframework.web.multipart.MultipartFile;

import java.util.*;

public class MemoryUserRepository implements UserRepository {
    private Map<String, User> users;

    public MemoryUserRepository() {
        this.users = new HashMap<>();
    }

    @Override
    public User createUser(String username, String email, String name, String password, String phoneNumber, Date birthDate, String profilePicUrl) {
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("Register failed - username:" + username + " already exists");
        }
        User user = new User(username, email, name, password, phoneNumber, birthDate,profilePicUrl);
        users.put(username, user);
        return user;
    }

    @Override
    public void removeUser(String username) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("Remove user failed - username:" + username + " doesn't exist");
        }
        users.remove(username);
    }

    @Override
    public void updateUserFields(String username, List<String> emails, String name, String password, String phoneNumber) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("Update failed - username: " + username + " doesn't exist");
        }
        User user = users.get(username);
        user.updateFields(emails, name, password, phoneNumber);
    }

    @Override
    public void updateUserFields(String username, List<String> emails, String name, String phoneNumber) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("Update failed - username: " + username + " doesn't exist");
        }
        User user = users.get(username);
        user.updateFields(emails, name, phoneNumber);
    }

    @Override
    public User getUser(String username) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("Username: " + username + " doesn't exist");
        }
        return users.get(username);
    }

    @Override
    public User getUserForCheck(String username) {
        if (!users.containsKey(username)) {
            return null;
        }
        return users.get(username);
    }

    @Override
    public void setAdmin(String username, boolean isAdmin) {
        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("Update failed - username: " + username + " doesn't exist");
        }
        User user = users.get(username);
        user.setAdmin(isAdmin);
    }

    @Override
    public void uploadCV(String username, MultipartFile pdfFile) {

    }

    @Override
    public byte[] getCV(String username) {
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void saveUser(User user) {
        return;
    }
    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String lowerCaseEmail = email.toLowerCase();
        return users.values().stream()
                .filter(user -> user.getEmails() != null && user.getEmails().contains(lowerCaseEmail))
                .findFirst();
    }
}
