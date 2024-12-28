package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.externalAIAPI.ProxyKeywordExtractor;
import com.dogood.dogoodbackend.domain.organizations.MemoryOrganizationRepository;
import com.dogood.dogoodbackend.domain.organizations.MemoryRequestRepository;
import com.dogood.dogoodbackend.domain.posts.MemoryVolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.reports.MemoryReportRepository;
import com.dogood.dogoodbackend.domain.volunteerings.MemoryVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.MemorySchedulingManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public FacadeManager facadeManager(){
        //this will memory for now but will actually db later
        //this is singleton
        return new FacadeManager(
                new MemoryVolunteeringRepository(),
                new MemoryOrganizationRepository(),
                new MemoryVolunteeringPostRepository(),
                new MemoryRequestRepository(),
                new MemoryReportRepository(),
                new MemorySchedulingManager(),
                new ProxyKeywordExtractor());
    }
}
