package com.dogood.dogoodbackend.domain.users.notificiations;

import java.util.List;

public interface NotificationRepository {
    public Notification createNotification(String usernameTo, String message, String navigationURL);
//    public void removeNotification(int notificationId);
    public Notification getNotification(int notificationId);
    public List<Notification> getUserNotifications(String username);
    public List<Notification> readNewUserNotifications(String username);
    public List<Notification> getAllNotifications();
}
