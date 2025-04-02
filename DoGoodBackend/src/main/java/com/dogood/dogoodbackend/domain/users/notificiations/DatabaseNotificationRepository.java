package com.dogood.dogoodbackend.domain.users.notificiations;

import com.dogood.dogoodbackend.jparepos.NotificationJPA;

import java.util.List;

public class DatabaseNotificationRepository implements NotificationRepository {
    private NotificationJPA jpa;

    public DatabaseNotificationRepository(NotificationJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public Notification createNotification(String usernameTo, String message, String navigationURL) {
        Notification notification = new Notification(usernameTo,message,navigationURL);
        jpa.save(notification);
        return notification;
    }

    @Override
    public Notification getNotification(int notificationId) {
        Notification notification = jpa.findById(notificationId).orElse(null);
        if (notification == null) {
            throw new IllegalArgumentException("Notification: " + notificationId + " doesn't exist");
        }
        return notification;
    }

    @Override
    public List<Notification> getUserNotifications(String username) {
        List<Notification> notifications = jpa.findByUsernameTo(username);
        return notifications;
    }

    @Override
    public List<Notification> readNewUserNotifications(String username) {
        List<Notification> notifications = jpa.findByUsernameToAndIsRead(username, false);
        for (Notification notification : notifications) {
            notification.markAsRead();
            jpa.save(notification);
        }
        return notifications;
    }

    @Override
    public List<Notification> getAllNotifications() {
        List<Notification> notifications = jpa.findAll();
        return notifications;
    }
}
