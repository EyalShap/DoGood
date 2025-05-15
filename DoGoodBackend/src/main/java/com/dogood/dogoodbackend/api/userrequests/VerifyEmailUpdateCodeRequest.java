package com.dogood.dogoodbackend.api.userrequests;

// UPDATE-EMAIL-VERIFICATION START
public class VerifyEmailUpdateCodeRequest {
    private String email; // The current email the code was sent to
    private String code;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
// UPDATE-EMAIL-VERIFICATION END
