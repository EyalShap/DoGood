package com.dogood.dogoodbackend.api.volunteeringrequests;

public class MakeCodeRequest {
    private int volunteeringId;
    private boolean constant;

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public void setVolunteeringId(int volunteeringId) {
        this.volunteeringId = volunteeringId;
    }

    public boolean isConstant() {
        return constant;
    }

    public void setConstant(boolean constant) {
        this.constant = constant;
    }
}
