package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.users.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserJPA extends JpaRepository<User, String> {
    Optional<User> findFirstByEmailsContains(String email); // Or a more specific query
}
