package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.service.VolunteeringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VolunteeringAPI {
    @Autowired
    VolunteeringService volunteeringService; //this is also singleton
}
