package com.dogood.dogoodbackend.domain.volunteerings;

public class PastExperience {
    String userId;
    String text;

    public PastExperience(String userId, String text) {
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
