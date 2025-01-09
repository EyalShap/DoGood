package com.dogood.dogoodbackend.domain.volunteerings;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Date;

@Embeddable
public class PastExperience {
    String userId;
    String text;

    @Column(name = "posted")
    Date when;

    public PastExperience(String userId, String text, Date when) {
        this.userId = userId;
        this.text = text;
        this.when =when;
    }

    public PastExperience() {

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
