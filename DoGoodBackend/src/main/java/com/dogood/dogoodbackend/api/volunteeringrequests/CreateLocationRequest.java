package com.dogood.dogoodbackend.api.volunteeringrequests;

import com.dogood.dogoodbackend.domain.volunteerings.AddressTuple;

public class CreateLocationRequest {
    private String name;
    private AddressTuple address;

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
}
