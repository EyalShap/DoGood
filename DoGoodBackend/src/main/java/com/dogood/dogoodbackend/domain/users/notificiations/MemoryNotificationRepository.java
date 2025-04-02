package com.dogood.dogoodbackend.domain.users.notificiations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryNotificationRepository implements NotificationRepository {
    private Map<Integer, Notification> notifications;
    private int nextNotificationId;

    public MemoryNotificationRepository() {
        notifications = new HashMap<>();
        nextNotificationId = 0;
    }

    @Override
    public Notification createNotification(String usernameTo, String message, String navigationURL) {
        Notification notification = new Notification(nextNotificationId, usernameTo, message, navigationURL);
        notifications.put(nextNotificationId, notification);
        nextNotificationId++;
        return notification;
    }

    @Override
    public Notification getNotification(int notificationId) {
        if (!notifications.containsKey(notificationId)) {
            throw new IllegalArgumentException("Notification: " + notificationId + " doesn't exist");
        }
        return notifications.get(notificationId);
    }

    @Override
    public List<Notification> getUserNotifications(String username) {
        List<Notification> result = new ArrayList<>();
        for (Notification notification : notifications.values()) {
            if (username != null && username.equals(notification.getUsernameTo())) {
                result.add(notification);
            }
        }
        return result;
    }

    @Override
    public List<Notification> readNewUserNotifications(String username) {
        List<Notification> result = new ArrayList<>();
        for (Notification notification : notifications.values()) {
            if (username != null && username.equals(notification.getUsernameTo()) && !notification.getIsRead()) {
                notification.markAsRead();
                result.add(notification);
            }
        }
        return result;
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notifications.values().stream().toList();
    }
}
