package com.dogood.dogoodbackend.domain.volunteerings;

public class LocationDTO {
    private final int id;
    private String name;
    private AddressTuple address;

    public LocationDTO(int id, String name, AddressTuple address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AddressTuple getAddress() {
        return address;
    }
}
