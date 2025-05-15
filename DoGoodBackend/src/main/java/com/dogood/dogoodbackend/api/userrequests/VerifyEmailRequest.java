package com.dogood.dogoodbackend.api.userrequests;

public class VerifyEmailRequest {
    private String username;
    private String code;

    public VerifyEmailRequest(String username, String code) {
        this.username = username;
        this.code = code;
    }

    public VerifyEmailRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}