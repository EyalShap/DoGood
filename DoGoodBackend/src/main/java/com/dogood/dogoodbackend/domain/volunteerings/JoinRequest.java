package com.dogood.dogoodbackend.domain.volunteerings;

import jakarta.persistence.Embeddable;

@Embeddable
public class JoinRequest {
    private String userId;
    private String text;

    public JoinRequest(String userId, String text) {
        this.userId = userId;
        this.text = text;
    }

    public JoinRequest() {

    }

    public String getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }
}
