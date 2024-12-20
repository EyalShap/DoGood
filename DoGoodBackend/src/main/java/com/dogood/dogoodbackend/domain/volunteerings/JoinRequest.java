package com.dogood.dogoodbackend.domain.volunteerings;

public class JoinRequest {
    String userId;
    String text;

    public JoinRequest(String userId, String text) {
        this.userId = userId;
        this.text = text;
    }

    public String getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }
}
