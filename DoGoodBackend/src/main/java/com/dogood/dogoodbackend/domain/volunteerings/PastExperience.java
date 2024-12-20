package com.dogood.dogoodbackend.domain.volunteerings;

import java.util.Date;

public class PastExperience {
    String userId;
    String text;
    Date when;

    public PastExperience(String userId, String text, Date when) {
        this.userId = userId;
        this.text = text;
        this.when =when;
    }

    public String getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }

    public Date getWhen() {
        return when;
    }
}
