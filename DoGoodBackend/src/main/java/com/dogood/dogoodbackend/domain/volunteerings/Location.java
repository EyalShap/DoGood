package com.dogood.dogoodbackend.domain.volunteerings;

public class Location {
    private final int id;
    private int volunteeringId;
    private String name;
    private AddressTuple address;

    public Location(int id, int volunteeringId, String name, AddressTuple address) {
        this.id = id;
        this.volunteeringId = volunteeringId;
        this.name = name;
        this.address = address;
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
