package com.dogood.dogoodbackend.api.userrequests;

public class RequestEmailUpdateVerificationRequest {
    private String email; // The current email of the user
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}