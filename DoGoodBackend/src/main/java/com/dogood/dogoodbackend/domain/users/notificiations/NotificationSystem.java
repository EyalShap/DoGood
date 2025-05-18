package com.dogood.dogoodbackend.domain.users.notificiations;

import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationSystem {
    private NotificationRepository repository;
    private NotificationSocketSender sender;
    private PushNotificationSender pushNotificationSender;

    public NotificationSystem(NotificationRepository repository) {
        this.repository = repository;
    }

    public void setSender(NotificationSocketSender sender) {
        this.sender = sender;
    }

    public void notifyUser(String username, String message, String navigationURL) {
        Notification notification = repository.createNotification(username, message, navigationURL);
        pushNotificationSender.sendPush(username,message,navigationURL);
        sender.sendNotification(username, notification);
    }

    public void setPushNotificationSender(PushNotificationSender pushNotificationSender) {
        this.pushNotificationSender = pushNotificationSender;
    }

    public List<Notification> getUserNotifications(String username) {
        return repository.getUserNotifications(username);
    }

    public List<Notification> readNewUserNotifications(String username) {
        return repository.readNewUserNotifications(username);
    }

    public Integer getNewUserNotificationsAmount(String username) {
        return repository.getUserNotifications(username).stream().filter(x-> !x.getIsRead()).toList().size();
    }
}
