package com.dogood.dogoodbackend.domain.users.notificiations;

import com.dogood.dogoodbackend.socket.NotificationSocketSender;

import java.util.List;

public class NotificationSystem {
    private NotificationRepository repository;
    private NotificationSocketSender sender;

    public NotificationSystem(NotificationRepository repository) {
        this.repository = repository;
    }

    public void setSender(NotificationSocketSender sender) {
        this.sender = sender;
    }

    public void notifyUser(String username, String message, String navigationURL) {
        Notification notification = repository.createNotification(username, message, navigationURL);
        sender.sendNotification(username, notification);
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
