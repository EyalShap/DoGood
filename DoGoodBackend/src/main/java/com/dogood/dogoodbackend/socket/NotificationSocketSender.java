package com.dogood.dogoodbackend.socket;


import com.dogood.dogoodbackend.domain.users.notificiations.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationSocketSender {
    @Autowired
    SimpMessagingTemplate template;

    public void sendNotification(String username, Notification notification){
        template.convertAndSendToUser(username, "/queue/notifications", notification);
    }

}
