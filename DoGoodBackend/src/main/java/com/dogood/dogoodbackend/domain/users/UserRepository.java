package com.dogood.dogoodbackend.domain.users;

import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    public User createUser(String username, String email, String name, String password, String phoneNumber, Date birthDate, String profilePicUrl);
    public void removeUser(String username);
    public void updateUserFields(String username, List<String> emails, String name, String password, String phoneNumber);
    public void updateUserFields(String username, List<String> emails, String name, String phoneNumber);
    public User getUser(String username);
    public List<User> getAllUsers();
    public void saveUser(User user);
    public void setAdmin(String username, boolean isAdmin);
    public void uploadCV(String username, MultipartFile pdfFile);
    public byte[] getCV(String username);
    Optional<User> findByUsername(String username); // New or ensure it exists: checks existence without throwing
    Optional<User> findByEmail(String email);    // New or ensure itexists: to check if email is used by another user.

//    public default List<UserDTO> getAllUserDTOs() {
//        List<User> users = getAllUsers();
//        List<UserDTO> userDTOS = users.stream()
//                .map(user -> new UserDTO(user))
//                .collect(Collectors.toList());
//        return userDTOS;
//    }
}
