package com.dogood.dogoodbackend.domain.volunteerings;

import jakarta.persistence.Embeddable;

@Embeddable
public class AddressTuple {
    private String city;
    private String street;
    private String address;

    public AddressTuple(String city, String street, String address) {
        this.city = city;
        this.street = street;
        this.address = address;
    }

    public AddressTuple(AddressTuple other) {
        this.city = other.getCity();
        this.street = other.getStreet();
        this.address = other.getAddress();
    }

    public AddressTuple() {

    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getAddress() {
        return address;
    }
}
