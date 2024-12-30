package com.dogood.dogoodbackend.api.reportquests;

public class CreateReportRequest {
    private String actor;
    private int reportedPostId;
    private String description;

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public int getReportedPostId() {
        return reportedPostId;
    }

    public void setReportedPostId(int reportedPostId) {
        this.reportedPostId = reportedPostId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
