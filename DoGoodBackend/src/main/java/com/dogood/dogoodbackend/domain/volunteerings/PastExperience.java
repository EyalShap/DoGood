package com.dogood.dogoodbackend.domain.volunteerings;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Date;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PastExperience that = (PastExperience) o;
        return Objects.equals(userId, that.userId) && Objects.equals(text, that.text) && Objects.equals(when, that.when);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, text, when);
    }
}
