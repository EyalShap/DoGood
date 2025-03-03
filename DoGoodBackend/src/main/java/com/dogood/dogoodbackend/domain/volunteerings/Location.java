package com.dogood.dogoodbackend.domain.volunteerings;

import jakarta.persistence.*;

@Entity
@IdClass(IdVolunteeringPK.class)
public class Location {

    @Id
    @Column(name="id")
    private int id;

    @Id
    @Column(name = "volunteering_id", nullable = false)
    private int volunteeringId;
    private String name;
    @Embedded
    private AddressTuple address;

    public Location(int id, int volunteeringId, String name, AddressTuple address) {
        this.id = id;
        this.volunteeringId = volunteeringId;
        this.name = name;
        this.address = address;
    }

    public Location() {

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AddressTuple getAddress() {
        return address;
    }

    public void setAddress(AddressTuple address) {
        this.address = address;
    }

    public LocationDTO getDTO(){
        return new LocationDTO(id, name, new AddressTuple(address));
    }
}
