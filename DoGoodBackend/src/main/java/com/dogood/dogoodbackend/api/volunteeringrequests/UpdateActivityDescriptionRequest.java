package com.dogood.dogoodbackend.api.volunteeringrequests;

import java.util.Date;

public class UpdateActivityDescriptionRequest {
    private Date start;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }
}
