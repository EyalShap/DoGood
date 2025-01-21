package com.dogood.dogoodbackend.domain.volunteerings;

import jakarta.persistence.Embeddable;

@Embeddable
public class IdVolunteeringPK {
    private int id;
    private int volunteeringId;

    public IdVolunteeringPK(int id, int volunteeringId) {
        this.id = id;
        this.volunteeringId = volunteeringId;
    }

    public IdVolunteeringPK() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public void setVolunteeringId(int volunteeringId) {
        this.volunteeringId = volunteeringId;
    }
}
