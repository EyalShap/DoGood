package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.users.notificiations.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationJPA extends JpaRepository<Notification, Integer> {
    public List<Notification> findByUsernameTo(String usernameTo);
    public List<Notification> findByUsernameToAndIsRead(String usernameTo, boolean isRead);
}
