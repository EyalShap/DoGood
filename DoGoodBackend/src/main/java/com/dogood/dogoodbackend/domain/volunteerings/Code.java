package com.dogood.dogoodbackend.domain.volunteerings;

import jakarta.persistence.Embeddable;

@Embeddable
public class Code {
    private String code;
    private long created;

    public Code(String code, long created) {
        this.code = code;
        this.created = created;
    }

    public Code() {

    }

    public String getCode() {
        return code;
    }

    public long getCreated() {
        return created;
    }
}
