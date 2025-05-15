package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.externalAIAPI.KeywordExtractor;
import com.dogood.dogoodbackend.domain.externalAIAPI.SkillsAndCategoriesExtractor;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.requests.RequestRepository;
import com.dogood.dogoodbackend.domain.users.MemoryUserRepository;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.UserRepository;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.volunteerings.MemoryVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringRepository;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest
public class OLDPostFacadeUnitTest extends AbstractPostsFacadeTest {
    @Mock
    private UsersFacade usersFacade;
    private UserRepository userRepository = new MemoryUserRepository();

    @Mock
    private VolunteeringFacade volunteeringFacade;
    private VolunteeringRepository volunteeringRepository = new MemoryVolunteeringRepository();

    @Mock
    private OrganizationsFacade organizationsFacade;

    @Mock
    private ReportsFacade reportsFacade;

    @Mock
    private NotificationSystem notificationSystem;

    @Mock
    private KeywordExtractor keywordExtractor;

    @Mock
    private SkillsAndCategoriesExtractor skillsAndCategoriesExtractor;

    @Mock
    private RequestRepository requestRepository;

    private PostsFacade postsFacade;

    @Override
    protected PostsFacade createPostsFacade() {
        return new PostsFacade(new MemoryVolunteeringPostRepository(), new MemoryVolunteerPostRepository(), usersFacade, volunteeringFacade, organizationsFacade, keywordExtractor, skillsAndCategoriesExtractor, requestRepository);
    }

    @Override
    protected UsersFacade createUsersFacade() {
        doAnswer(invocation -> {
            userRepository.createUser(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2), invocation.getArgument(3), invocation.getArgument(4), invocation.getArgument(5), "");
            return null;
        }).when(usersFacade).register(anyString(), anyString(), anyString(), anyString(), anyString(), any(Date.class));

        doAnswer(invocation -> {
            try {
                User user = userRepository.getUser(invocation.getArgument(0));
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }).when(usersFacade).userExists(anyString());

        doAnswer(invocation -> {
            User user = userRepository.getUser(invocation.getArgument(0));
            user.updateSkills(invocation.getArgument(1));
            return null;
        }).when(usersFacade).updateUserSkills(anyString(), anyList());

        return usersFacade;
    }

    @Override
    protected VolunteeringFacade createVolunteeringFacade() {
        doAnswer(invocation -> volunteeringRepository.getVolunteering(invocation.getArgument(0)).getDTO()).when(volunteeringFacade).getVolunteeringDTO(anyInt());
        doAnswer(invocation -> volunteeringRepository.getVolunteering(invocation.getArgument(0)).getSkills()).when(volunteeringFacade).getVolunteeringSkills(anyInt());
        doAnswer(invocation -> volunteeringRepository.getVolunteering(invocation.getArgument(0)).getCategories()).when(volunteeringFacade).getVolunteeringCategories(anyInt());
        doAnswer(invocation -> volunteeringRepository.getVolunteering(invocation.getArgument(0)).getLocationDTOs()).when(volunteeringFacade).getVolunteeringLocations(anyInt());
        doAnswer(invocation -> volunteeringRepository.getVolunteering(invocation.getArgument(0)).getPastExperiences()).when(volunteeringFacade).getVolunteeringPastExperiences(anyInt());

        //when(volunteeringFacade.denyUserJoinRequest();).denyUserJoinRequest(anyString(), anyInt(), anyString());

        return volunteeringFacade;
    }

    @Override
    protected VolunteeringFacade createSpyVolunteeringFacade() {
        return volunteeringFacade;
    }

    @Override
    protected OrganizationsFacade createOrganizationsFacade() {
        return organizationsFacade;
    }

    @Override
    protected OrganizationsFacade createSpyOrganizationsFacade() {
        return organizationsFacade;
    }

    @Override
    protected ReportsFacade createReportsFacade() {
        return reportsFacade;
    }

    @Override
    protected ReportsFacade createSpyReportsFacade() {
        return reportsFacade;
    }

    @Override
    protected NotificationSystem createNotificationSystem() {
        return notificationSystem;
    }
}
