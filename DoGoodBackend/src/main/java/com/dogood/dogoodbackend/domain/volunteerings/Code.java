package com.dogood.dogoodbackend.domain.volunteerings;

import java.util.Date;

public class Code {
    private String code;
    private long created;

    public Code(String code, long created) {
        this.code = code;
        this.created = created;
    }

    public String getCode() {
        return code;
    }

    public long getCreated() {
        return created;
    }
}
