package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJPA extends JpaRepository<User, String> {
}
