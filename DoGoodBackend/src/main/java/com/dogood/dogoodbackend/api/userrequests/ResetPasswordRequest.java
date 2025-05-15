// FORGOT_PASSWORD START
package com.dogood.dogoodbackend.api.userrequests;
public class ResetPasswordRequest {
    private String username;
    private String newPassword;
    private String code; // Added code for re-validation at the point of reset
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
// FORGOT_PASSWORD END