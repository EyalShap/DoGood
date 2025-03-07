package com.dogood.dogoodbackend.domain.requests;

public enum RequestObject {
    ORGANIZATION("organization"), VOLUNTEER_POST("volunteer post");

    private String str;

    RequestObject(String str) {
        this.str = str;
    }

    public String getString() {
        return str;
    }
}
