package com.dogood.dogoodbackend.domain.reports;

import jakarta.persistence.*;

@Entity
@Table(name = "banned")
public class Banned {
    @Id
    @Column(name = "email")
    private String email;

    public Banned(){}

    public Banned(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
