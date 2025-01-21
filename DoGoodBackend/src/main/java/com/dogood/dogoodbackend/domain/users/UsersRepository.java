package com.dogood.dogoodbackend.domain.users;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public interface UsersRepository {
    public User createUser(String username, String email, String name, String password, String phoneNumber, Date birthDate);
    public void removeUser(String username);
    public void updateUserFields(String username, List<String> emails, String name, String password, String phoneNumber);
    public User getUser(String username);
    public List<User> getAllUsers();

//    public default List<UserDTO> getAllUserDTOs() {
//        List<User> users = getAllUsers();
//        List<UserDTO> userDTOS = users.stream()
//                .map(user -> new UserDTO(user))
//                .collect(Collectors.toList());
//        return userDTOS;
//    }
}
