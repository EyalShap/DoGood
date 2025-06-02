package com.dogood.dogoodbackend.domain.reports;

public enum ReportObject {
    VOLUNTEERING_POST("Volunteering Post"),
    VOLUNTEER_POST("Volunteer Post"),
    ORGANIZATION("Organization"),
    USER("User"),
    VOLUNTEERING("Volunteering");

    private final String displayName;

    ReportObject(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
