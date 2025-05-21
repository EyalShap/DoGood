package com.dogood.dogoodbackend.api.userrequests;

// FORGOT_PASSWORD START
public class ForgotPasswordRequest {
    private String username; // Changed from email to username

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
// FORGOT_PASSWORD END