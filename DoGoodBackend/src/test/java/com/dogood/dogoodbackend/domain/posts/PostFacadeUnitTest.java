package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.externalAIAPI.AI;
import com.dogood.dogoodbackend.domain.externalAIAPI.KeywordExtractor;
import com.dogood.dogoodbackend.domain.externalAIAPI.SkillsAndCategoriesExtractor;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.requests.RequestRepository;
import com.dogood.dogoodbackend.domain.users.MemoryUserRepository;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.UserRepository;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.Notification;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.volunteerings.MemoryVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringRepository;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class PostFacadeUnitTest {
    @Mock
    private UsersFacade usersFacade;
    private UserRepository userRepository = new MemoryUserRepository();

    @Mock
    private VolunteeringFacade volunteeringFacade;

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

    @Mock
    private AI ai;

    @Mock
    private NotificationSocketSender sender;

    private PostsFacade postsFacade;

    private int organizationId = 123;
    private int volunteeringId1 = 1;
    private int volunteeringId2 = 2;
    private int volunteeringPostId, volunteerPostId;
    private final String title = "Postito";
    private final String description = "Postito is a very cool post";
    private final String posterUsername = "Poster";
    private final String otherManagerUsername = "Manager";
    private final String nonPosterUsername = "NonPoster";
    private final String admin = "Admin";
    private final String newUser = "NewUser";

    void setUsersFacadeBehavior() {
        doAnswer(invocation -> {
            userRepository.createUser(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2), invocation.getArgument(3), invocation.getArgument(4), invocation.getArgument(5), "");
            return null;
        }).when(usersFacade).register(anyString(), anyString(), anyString(), anyString(), anyString(), any(Date.class));

        doAnswer(invocation -> {
            userRepository.createUser(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2), invocation.getArgument(3), invocation.getArgument(4), invocation.getArgument(5), "");
            userRepository.setAdmin(invocation.getArgument(0), true);
            return null;
        }).when(usersFacade).registerAdmin(anyString(), anyString(), anyString(), anyString(), anyString(), any(Date.class));

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

        when(usersFacade.isAdmin(eq(admin))).thenReturn(true);
    }

    @BeforeEach
    void setUp() {
        this.postsFacade = new PostsFacade(new MemoryVolunteeringPostRepository(), new MemoryVolunteerPostRepository(), usersFacade, volunteeringFacade, organizationsFacade, keywordExtractor, skillsAndCategoriesExtractor, requestRepository);

        postsFacade.setReportsFacade(reportsFacade);
        postsFacade.setOrganizationsFacade(organizationsFacade);
        postsFacade.setKeywordExtractor(keywordExtractor);
        postsFacade.setSkillsAndCategoriesExtractor(skillsAndCategoriesExtractor);
        postsFacade.setVolunteeringFacade(volunteeringFacade);

        setUsersFacadeBehavior();
        usersFacade.register(posterUsername, "password", "Moshe Cohen", "moshe@gmail.com", "0541967544", new Date());
        usersFacade.register(otherManagerUsername, "password", "Yossi Levi", "yossi@gmail.com", "0541967544", new Date());
        usersFacade.register(nonPosterUsername, "password", "Miriam Cohen", "miriam@gmail.com", "0541967544", new Date());
        usersFacade.registerAdmin(admin, "password", "Admin Cohen", "moshe@gmail.com", "0541967544", new Date());
        usersFacade.updateUserSkills(posterUsername, List.of("keyword1"));
        usersFacade.updateUserSkills(otherManagerUsername, List.of("keyword2"));
        usersFacade.updateUserSkills(nonPosterUsername, List.of("keyword3"));
        usersFacade.updateUserSkills(admin, List.of("keyword5"));

        when(volunteeringFacade.getVolunteeringDTO(volunteeringId1)).thenReturn(new VolunteeringDTO(volunteeringId1, organizationId, "Volunteering1", "This is a very cool volunteering.", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        when(volunteeringFacade.getVolunteeringDTO(volunteeringId2)).thenReturn(new VolunteeringDTO(volunteeringId2, organizationId, "Volunteering2", "This is a very cool volunteering.", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        when(volunteeringFacade.getVolunteeringDTO(anyInt())).thenAnswer(invocation -> {
            int id = invocation.getArgument(0);
            if (!(id == volunteeringId1 || id == volunteeringId2)) {
                throw new IllegalArgumentException("Volunteering with id " + id + " does not exist");
            }
            return true;
        });

        when(organizationsFacade.isManager(Mockito.anyString(), eq(organizationId))).thenReturn(false);
        when(organizationsFacade.isManager(eq(posterUsername), eq(organizationId))).thenReturn(true);
        when(organizationsFacade.isManager(eq(otherManagerUsername), eq(organizationId))).thenReturn(true);

        this.volunteeringPostId = postsFacade.createVolunteeringPost(title, description, posterUsername, volunteeringId1);
        this.volunteerPostId = postsFacade.createVolunteerPost(title, description, posterUsername);
        postsFacade.sendAddRelatedUserRequest(volunteerPostId, otherManagerUsername, posterUsername);
        postsFacade.handleAddRelatedUserRequest(volunteerPostId, otherManagerUsername, true);

        reset(sender);
        reset(organizationsFacade);
        reset(reportsFacade);
        reset(keywordExtractor);
        reset(skillsAndCategoriesExtractor);
    }

    private static Stream<Arguments> validVolunteeringPostInputs() {
        return Stream.of(
                Arguments.of("Help with Animals", "We need volunteers to care for rescued dogs.", "Poster"),
                Arguments.of("Food Drive", "Join us in organizing a local food drive event.", "Poster"),
                Arguments.of("Beach Cleanup", "Help clean up the beach and protect marine life.", "Poster"),
                Arguments.of("Tutoring Kids", "Volunteer to tutor kids in math and reading.", "Poster"),
                Arguments.of("Art Therapy", "Support art therapy workshops for mental health.", "Poster"),
                Arguments.of("Help with Animals", "We need volunteers to care for rescued dogs.", "Manager"),
                Arguments.of("Food Drive", "Join us in organizing a local food drive event.", "Manager"),
                Arguments.of("Beach Cleanup", "Help clean up the beach and protect marine life.", "Manager"),
                Arguments.of("Tutoring Kids", "Volunteer to tutor kids in math and reading.", "Manager"),
                Arguments.of("Art Therapy", "Support art therapy workshops for mental health.", "Manager"),
                Arguments.of("Help with Animals", "We need volunteers to care for rescued dogs.", "Admin"),
                Arguments.of("Food Drive", "Join us in organizing a local food drive event.", "Admin"),
                Arguments.of("Beach Cleanup", "Help clean up the beach and protect marine life.", "Admin"),
                Arguments.of("Tutoring Kids", "Volunteer to tutor kids in math and reading.", "Admin"),
                Arguments.of("Art Therapy", "Support art therapy workshops for mental health.", "Admin")
        );
    }

    @ParameterizedTest
    @MethodSource("validVolunteeringPostInputs")
    void givenValidFields_whenCreateVolunteeringPost_thenCreate(String title, String description, String actor) {
        when(ai.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");

        when(volunteeringFacade.getVolunteeringOrganizationId(volunteeringId1)).thenReturn(organizationId);

        int numPostsBefore = postsFacade.getAllVolunteeringPosts(posterUsername).size();

        postsFacade.createVolunteeringPost(title, description, actor, volunteeringId1);
        verify(keywordExtractor).getVolunteeringPostKeywords(eq("Volunteering1"), eq("This is a very cool volunteering."), eq(title), eq(description));

        String newPostMessage = String.format("The new post \"%s\" might be relevant for you!", title);
        String managersMessage = String.format("The new post \"%s\" was created for the volunteering \"%s\" in your organization \"%s\".", title, "Volunteering1", "Organization");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(organizationsFacade).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());

        verify(sender, times(5)).sendNotification(recipientCaptor.capture(), captor.capture());

        List<String> recipients = recipientCaptor.getAllValues();
        List<Notification> notifications = captor.getAllValues();

        /*assertTrue(recipients.subList(0, 3).containsAll(List.of(posterUsername, otherManagerUsername, nonPosterUsername)));
        for (Notification notification : notifications.subList(0, 3)) {
            assertEquals(newPostMessage, notification.getMessage());
        }*/

        assertTrue(recipients.subList(3, 5).containsAll(List.of(posterUsername, otherManagerUsername)));
        for (Notification notification : notifications.subList(3, 5)) {
            assertEquals(managersMessage, notification.getMessage());
        }

        int numPostsAfter = postsFacade.getAllVolunteeringPosts(posterUsername).size();
        assertEquals(1, numPostsAfter - numPostsBefore);
    }
}
