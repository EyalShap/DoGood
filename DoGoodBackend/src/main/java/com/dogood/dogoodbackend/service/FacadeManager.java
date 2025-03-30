package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.chat.ChatFacade;
import com.dogood.dogoodbackend.domain.chat.MessageRepository;
import com.dogood.dogoodbackend.domain.externalAIAPI.KeywordExtractor;
import com.dogood.dogoodbackend.domain.externalAIAPI.SkillsAndCategoriesExtractor;
import com.dogood.dogoodbackend.domain.organizations.*;
import com.dogood.dogoodbackend.domain.requests.RequestRepository;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.posts.VolunteerPostRepository;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.reports.ReportRepository;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.UserRepository;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.SchedulingManager;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import org.springframework.beans.factory.annotation.Autowired;

public class FacadeManager {
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;
    private PostsFacade postsFacade;
    private ReportsFacade reportsFacade;
    private UsersFacade usersFacade;
    private AuthFacade authFacade;
    private ChatFacade chatFacade;

    public FacadeManager(String jwtSecretKey, VolunteeringRepository volRepo, OrganizationRepository orgRepo, VolunteeringPostRepository volunteeringPostRepo, VolunteerPostRepository volunteerPostRepo,
                         RequestRepository reqRepo, ReportRepository repRepo, UserRepository userRepo, SchedulingManager schedMan, KeywordExtractor keyExt, SkillsAndCategoriesExtractor skillsCatExt, MessageRepository messageRepository){
        this.authFacade = new AuthFacade(jwtSecretKey);
        this.usersFacade = new UsersFacade(userRepo, authFacade);

        this.organizationsFacade = new OrganizationsFacade(usersFacade, orgRepo, reqRepo);
        this.volunteeringFacade = new VolunteeringFacade(usersFacade, this.organizationsFacade, volRepo, schedMan, skillsCatExt);
        this.postsFacade = new PostsFacade(volunteeringPostRepo, volunteerPostRepo, usersFacade, volunteeringFacade, organizationsFacade, keyExt, skillsCatExt, reqRepo);
        this.reportsFacade = new ReportsFacade(usersFacade, repRepo, postsFacade, volunteeringFacade, organizationsFacade);
        this.organizationsFacade.setVolunteeringFacade(volunteeringFacade);
        this.postsFacade.setReportsFacade(reportsFacade);
        this.volunteeringFacade.setReportFacade(reportsFacade);
        this.organizationsFacade.setReportFacade(reportsFacade);
        this.volunteeringFacade.setPostsFacade(postsFacade);
        this.usersFacade.setVolunteeringFacade(volunteeringFacade);
        this.chatFacade = new ChatFacade(volunteeringFacade, postsFacade, messageRepository);
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
}
