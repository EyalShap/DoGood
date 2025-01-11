package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.externalAIAPI.ProxyKeywordExtractor;
import com.dogood.dogoodbackend.domain.organizations.DBOrganizationRepository;
import com.dogood.dogoodbackend.domain.organizations.DBRequestRepository;
import com.dogood.dogoodbackend.domain.organizations.MemoryOrganizationRepository;
import com.dogood.dogoodbackend.domain.organizations.MemoryRequestRepository;
import com.dogood.dogoodbackend.domain.posts.MemoryVolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.reports.MemoryReportRepository;
import com.dogood.dogoodbackend.domain.volunteerings.DatabaseVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.MemoryVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.DatabaseSchedulingManager;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.MemorySchedulingManager;
import com.dogood.dogoodbackend.jparepos.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public FacadeManager facadeManager(ApplicationContext applicationContext){
        //this will memory for now but will actually db later
        //this is singleton
        return new FacadeManager(
                new DatabaseVolunteeringRepository(applicationContext.getBean(VolunteeringJPA.class)),
                new DBOrganizationRepository(applicationContext.getBean(OrganizationJPA.class)),
                new MemoryVolunteeringPostRepository(),
                new DBRequestRepository(applicationContext.getBean(RequestJPA.class)),
                new MemoryReportRepository(),
                new DatabaseSchedulingManager(
                        applicationContext.getBean(HourRequestJPA.class),
                        applicationContext.getBean(AppointmentJPA.class),
                        applicationContext.getBean(ApprovedHoursJPA.class)),
                new ProxyKeywordExtractor());
    }

    /*@Bean
    public FacadeManager facadeManager(ApplicationContext applicationContext){
        //this will memory for now but will actually db later
        //this is singleton
        return new FacadeManager(
                new DatabaseVolunteeringRepository(applicationContext.getBean(VolunteeringJPA.class)),
                new MemoryOrganizationRepository(),
                new MemoryVolunteeringPostRepository(),
                new MemoryRequestRepository(),
                new MemoryReportRepository(),
                new DatabaseSchedulingManager(
                        applicationContext.getBean(HourRequestJPA.class),
                        applicationContext.getBean(AppointmentJPA.class),
                        applicationContext.getBean(ApprovedHoursJPA.class)),
                new ProxyKeywordExtractor());
    }*/
}
