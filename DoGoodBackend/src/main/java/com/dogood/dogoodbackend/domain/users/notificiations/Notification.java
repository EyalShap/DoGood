package com.dogood.dogoodbackend.domain.users.notificiations;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String usernameTo;
    private String message;
    private String navigationURL;
    private boolean isRead;
    private Date timestamp;

    public Notification() {
    }

    public Notification(String usernameTo, String message, String navigationURL) {
        this.usernameTo = usernameTo;
        this.message = message;
        this.navigationURL = navigationURL;
        this.isRead = false;
        this.timestamp = new Date();
    }

    public Notification(int id, String usernameTo, String message, String navigationURL) {
        this.id = id;
        this.usernameTo = usernameTo;
        this.message = message;
        this.navigationURL = navigationURL;
        this.isRead = false;
        this.timestamp = new Date();
    }

    public int getId() {
        return id;
    }

    public Notification setId(int id) {
        this.id = id;
        return this;
    }

    public String getUsernameTo() {
        return usernameTo;
    }

    public Notification setUsernameTo(String usernameTo) {
        this.usernameTo = usernameTo;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Notification setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getNavigationURL() {
        return navigationURL;
    }

    public Notification setNavigationURL(String navigationURL) {
        this.navigationURL = navigationURL;
        return this;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public boolean getIsRead() {
        return this.isRead;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }
}
