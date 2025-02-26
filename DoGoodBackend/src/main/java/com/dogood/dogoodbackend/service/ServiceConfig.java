package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.externalAIAPI.ProxyKeywordExtractor;
import com.dogood.dogoodbackend.domain.organizations.DBOrganizationRepository;
import com.dogood.dogoodbackend.domain.organizations.DBRequestRepository;
import com.dogood.dogoodbackend.domain.posts.DBVolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.reports.DBReportRepository;
import com.dogood.dogoodbackend.domain.users.DatabaseUserRepository;
import com.dogood.dogoodbackend.domain.users.MemoryUserRepository;
import com.dogood.dogoodbackend.domain.volunteerings.DatabaseVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.DatabaseSchedulingManager;
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
        return new FacadeManager(applicationContext.getEnvironment().getProperty("security.jwt.secret-key"),
                new DatabaseVolunteeringRepository(applicationContext.getBean(VolunteeringJPA.class)),
                new DBOrganizationRepository(applicationContext.getBean(OrganizationJPA.class)),
                new DBVolunteeringPostRepository(applicationContext.getBean(VolunteeringPostJPA.class)),
                new DBRequestRepository(applicationContext.getBean(RequestJPA.class)),
                new DBReportRepository(applicationContext.getBean(ReportJPA.class)),
                new DatabaseUserRepository(applicationContext.getBean(UserJPA.class)),
                new DatabaseSchedulingManager(
                        applicationContext.getBean(HourRequestJPA.class),
                        applicationContext.getBean(AppointmentJPA.class)),
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
                new MemoryUsersRepository(),
                new MemorySchedulingManager(),
                new DatabaseSchedulingManager(
                        applicationContext.getBean(HourRequestJPA.class),
                        applicationContext.getBean(AppointmentJPA.class),
                        applicationContext.getBean(ApprovedHoursJPA.class)),
                new ProxyKeywordExtractor());
    }*/
}
