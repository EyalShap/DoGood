// FORGOT_PASSWORD START
package com.dogood.dogoodbackend.api.userrequests;

public class VerifyResetPasswordRequest {
    private String username;
    private String code;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
// FORGOT_PASSWORD END