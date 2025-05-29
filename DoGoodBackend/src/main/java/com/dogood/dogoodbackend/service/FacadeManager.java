package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.chat.ChatFacade;
import com.dogood.dogoodbackend.domain.chat.MessageRepository;
import com.dogood.dogoodbackend.domain.externalAIAPI.CVSkillsAndPreferencesExtractor;
import com.dogood.dogoodbackend.domain.externalAIAPI.KeywordExtractor;
import com.dogood.dogoodbackend.domain.externalAIAPI.SkillsAndCategoriesExtractor;
import com.dogood.dogoodbackend.domain.organizations.*;
import com.dogood.dogoodbackend.domain.reports.BannedRepository;
import com.dogood.dogoodbackend.domain.reports.EmailBanner;
import com.dogood.dogoodbackend.domain.requests.RequestRepository;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.posts.VolunteerPostRepository;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.reports.ReportRepository;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.users.UserRegisterer;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.UserRepository;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationRepository;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.SchedulingManager;
// VERIFICATION START
// No changes needed for imports here unless EmailService or VerificationCacheService were not previously imported
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.pdfformats.PdfFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
// VERIFICATION END

public class FacadeManager {
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;
    private PostsFacade postsFacade;
    private ReportsFacade reportsFacade;
    private UsersFacade usersFacade;
    private AuthFacade authFacade;
    private ChatFacade chatFacade;
    private NotificationSystem notificationSystem;

    public FacadeManager(UserRegisterer userRegisterer, String jwtSecretKey, VolunteeringRepository volRepo, OrganizationRepository orgRepo, VolunteeringPostRepository volunteeringPostRepo, VolunteerPostRepository volunteerPostRepo,
                         RequestRepository reqRepo, ReportRepository repRepo, BannedRepository bannedRepo, UserRepository userRepo, SchedulingManager schedMan, KeywordExtractor keyExt, SkillsAndCategoriesExtractor skillsCatExt, CVSkillsAndPreferencesExtractor cvExt, MessageRepository messageRepository, NotificationRepository notificationRepo, EmailSender emailSender, VerificationCacheService verificationCacheService, PdfFactory pdfFactory, EmailBanner emailBanner){
        this.authFacade = new AuthFacade(jwtSecretKey);
        this.usersFacade = new UsersFacade(userRepo, authFacade, cvExt, emailSender, verificationCacheService);
        this.usersFacade.setUserRegisterer(userRegisterer);
        this.notificationSystem = new NotificationSystem(notificationRepo);
        this.organizationsFacade = new OrganizationsFacade(usersFacade, orgRepo, reqRepo);
        this.volunteeringFacade = new VolunteeringFacade(usersFacade, this.organizationsFacade, volRepo, schedMan, skillsCatExt,pdfFactory);
        this.postsFacade = new PostsFacade(volunteeringPostRepo, volunteerPostRepo, usersFacade, volunteeringFacade, organizationsFacade, keyExt, skillsCatExt, reqRepo);
        this.reportsFacade = new ReportsFacade(usersFacade, repRepo, bannedRepo, postsFacade, volunteeringFacade, organizationsFacade);
        this.organizationsFacade.setVolunteeringFacade(volunteeringFacade);
        this.postsFacade.setReportsFacade(reportsFacade);
        this.volunteeringFacade.setReportFacade(reportsFacade);
        this.organizationsFacade.setReportFacade(reportsFacade);
        this.organizationsFacade.setNotificationSystem(notificationSystem);
        this.volunteeringFacade.setPostsFacade(postsFacade);
        this.volunteeringFacade.setNotificationSystem(notificationSystem);
        this.usersFacade.setVolunteeringFacade(volunteeringFacade);
        this.usersFacade.setReportsFacade(reportsFacade);
        this.usersFacade.setNotificationSystem(notificationSystem);
        this.chatFacade = new ChatFacade(volunteeringFacade, postsFacade, messageRepository);
        this.chatFacade.setNotificationSystem(notificationSystem);
        this.postsFacade.setNotificationSystem(notificationSystem);
        this.reportsFacade.setEmailBanner(emailBanner);
    }

    public VolunteeringFacade getVolunteeringFacade() {
        return volunteeringFacade;
    }

    public OrganizationsFacade getOrganizationsFacade() {
        return organizationsFacade;
    }

    public PostsFacade getPostsFacade() {
        return postsFacade;
    }

    public ReportsFacade getReportsFacade() {
        return reportsFacade;
    }

    public AuthFacade getAuthFacade() {
        return authFacade;
    }

    public UsersFacade getUsersFacade() {
        return usersFacade;
    }

    public ChatFacade getChatFacade() {
        return chatFacade;
    }

    public NotificationSystem getNotificationSystem() {
        return notificationSystem;
    }

}
