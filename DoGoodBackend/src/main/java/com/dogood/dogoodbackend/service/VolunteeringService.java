package com.dogood.dogoodbackend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VolunteeringService {

    private FacadeManager facadeManager;

    @Autowired
    public VolunteeringService(FacadeManager facadeManager){
        this.facadeManager = facadeManager;
    }
}
