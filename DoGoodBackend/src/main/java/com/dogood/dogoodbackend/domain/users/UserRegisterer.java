package com.dogood.dogoodbackend.domain.users;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Date;

@Service
public class UserRegisterer {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User registerUser(UserRepository repository, String username, String password, String name, String email, String phone, Date birthDate, String profilePicUrl) {
        User user = repository.getUserForCheck(username);
        if(user != null){
            throw new IllegalArgumentException("The username " + username + " is already in use.");
        }
        return repository.createUser(username, email, name, password, phone, birthDate,profilePicUrl);
    }
}
