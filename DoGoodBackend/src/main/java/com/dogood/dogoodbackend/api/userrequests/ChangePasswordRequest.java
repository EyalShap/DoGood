package com.dogood.dogoodbackend.api.userrequests;
public class ChangePasswordRequest {
    private String username; // Username might be redundant if derived from token, but included as per frontend spec
    private String oldPassword;
    private String newPassword;
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
// PASSWORD-CHANGE-NO-EMAIL END
