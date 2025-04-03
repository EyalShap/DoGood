package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.chat.DatabaseMessageRepository;
import com.dogood.dogoodbackend.domain.externalAIAPI.*;
import com.dogood.dogoodbackend.domain.organizations.DBOrganizationRepository;
import com.dogood.dogoodbackend.domain.reports.DBBannedRepository;
import com.dogood.dogoodbackend.domain.requests.DBRequestRepository;
import com.dogood.dogoodbackend.domain.posts.DBVolunteerPostRepository;
import com.dogood.dogoodbackend.domain.posts.DBVolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.reports.DBReportRepository;
import com.dogood.dogoodbackend.domain.users.DatabaseUserRepository;
import com.dogood.dogoodbackend.domain.users.notificiations.DatabaseNotificationRepository;
import com.dogood.dogoodbackend.domain.volunteerings.DatabaseVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.DatabaseSchedulingManager;
import com.dogood.dogoodbackend.jparepos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {
    @Bean
    public FacadeManager facadeManager(ApplicationContext applicationContext){
        //this is singleton
        return new FacadeManager(applicationContext.getEnvironment().getProperty("security.jwt.secret-key"),
                new DatabaseVolunteeringRepository(applicationContext.getBean(VolunteeringJPA.class)),
                new DBOrganizationRepository(applicationContext.getBean(OrganizationJPA.class)),
                new DBVolunteeringPostRepository(applicationContext.getBean(VolunteeringPostJPA.class)),
                new DBVolunteerPostRepository(applicationContext.getBean(VolunteerPostJPA.class)),
                new DBRequestRepository(applicationContext.getBean(RequestJPA.class)),
                new DBReportRepository(applicationContext.getBean(ReportJPA.class)),
                new DBBannedRepository(applicationContext.getBean(BannedJPA.class)),
                new DatabaseUserRepository(applicationContext.getBean(UserJPA.class)),
                new DatabaseSchedulingManager(
                        applicationContext.getBean(HourRequestJPA.class),
                        applicationContext.getBean(AppointmentJPA.class)),
                new AIKeywordExtractor(applicationContext.getBean(Gemini.class)),
                new AISkillsAndCategoriesExtractor(applicationContext.getBean(Gemini.class)),
                new AICVSkillsAndPreferencesExtractor(applicationContext.getBean(Gemini.class)),
                new DatabaseMessageRepository(applicationContext.getBean(MessageJPA.class)),
                new DatabaseNotificationRepository(applicationContext.getBean(NotificationJPA.class)));
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
