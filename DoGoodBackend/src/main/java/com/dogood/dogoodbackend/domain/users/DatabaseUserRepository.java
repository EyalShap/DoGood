package com.dogood.dogoodbackend.domain.users;

import com.dogood.dogoodbackend.jparepos.UserJPA;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class DatabaseUserRepository implements UserRepository {
    private UserJPA jpa;

    public DatabaseUserRepository(UserJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public User createUser(String username, String email, String name, String password, String phoneNumber, Date birthDate, String profilePicUrl) {
        if (jpa.findById(username).orElse(null) != null) {
            throw new IllegalArgumentException("Register failed - username:" + username + " already exists");
        }
        User user = new User(username, email, name, password, phoneNumber, birthDate,profilePicUrl);
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
            throw new IllegalArgumentException("Update failed - username: " + username + " doesn't exist");
        }
        user.updateFields(emails, name, password, phoneNumber);
        jpa.save(user);
    }

    @Override
    public void updateUserFields(String username, List<String> emails, String name, String phoneNumber) {
        User user = jpa.findById(username).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Update failed - username: " + username + " doesn't exist");
        }
        user.updateFields(emails, name, phoneNumber);
        jpa.save(user);
    }

    @Override
    public User getUserForCheck(String username) {
        return jpa.findById(username).orElse(null);
    }

    @Override
    public User getUser(String username) {
        User user = jpa.findById(username).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Username: " + username + " doesn't exist");
        }
        return user;
    }

    @Override
    public void setAdmin(String username, boolean isAdmin) {
        User user = jpa.findById(username).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Update failed - username: " + username + " doesn't exist");
        }
        user.setAdmin(isAdmin);
        jpa.save(user);
    }

    @Override
    public void uploadCV(String username, MultipartFile cvPdf) {
        User user = jpa.findById(username).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Update failed - username: " + username + " doesn't exist");
        }
        try {
            byte[] cvBytes = cvPdf != null ? cvPdf.getBytes() : null;
            user.uploadCV(cvBytes);
            jpa.save(user);
        }
        catch (IOException exception) {
            throw new IllegalArgumentException("Problem uploading cv.");
        }
    }

    @Override
    public byte[] getCV(String username) {
        User user = jpa.findById(username).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Update failed - username: " + username + " doesn't exist");
        }
        byte[] cv = user.getCv();
        if(cv == null) {
            throw new IllegalArgumentException("No CV.");
        }
        return cv;
    }

    @Override
    public List<User> getAllUsers() {
        return jpa.findAll();
    }

    @Override
    public void saveUser(User user) {
        jpa.save(user);
    }
    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findById(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // This requires a method in your UserJPA interface, e.g.,
        // @Query("SELECT u FROM User u WHERE ?1 MEMBER OF u.emails")
        // Optional<User> findUserByEmailInEmails(String email);
        // Or, if 'emails' list typically contains one primary email for new users:
        return jpa.findFirstByEmailsContains(email.toLowerCase());
        // If UserJPA does not have such a method, you might need to findAll and filter,
        // but that's inefficient. It's best to add a query to UserJPA.
    }
}
