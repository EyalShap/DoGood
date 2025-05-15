package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.externalAIAPI.AI;
import com.dogood.dogoodbackend.domain.externalAIAPI.KeywordExtractor;
import com.dogood.dogoodbackend.domain.externalAIAPI.SkillsAndCategoriesExtractor;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.requests.RequestObject;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.Notification;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.volunteerings.AddressTuple;
import com.dogood.dogoodbackend.domain.volunteerings.PastExperience;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import com.dogood.dogoodbackend.utils.PostErrors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractPostsFacadeTest {
    protected PostsFacade postsFacade;
    protected UsersFacade usersFacade;
    protected VolunteeringFacade volunteeringFacade;
    protected VolunteeringFacade spyVolunteeringFacade;
    protected OrganizationsFacade organizationsFacade;
    protected OrganizationsFacade spyOrganizationsFacade;
    protected ReportsFacade reportsFacade;
    protected ReportsFacade spyReportsFacade;
    protected KeywordExtractor spyKeywordExtractor;
    protected SkillsAndCategoriesExtractor spySkillsAndCategoriesExtractor;
    protected NotificationSystem notificationSystem;

    @Mock
    private AI ai;

    @Mock
    private NotificationSocketSender sender;

    protected abstract PostsFacade createPostsFacade();
    protected abstract UsersFacade createUsersFacade();
    protected abstract VolunteeringFacade createVolunteeringFacade();
    protected abstract VolunteeringFacade createSpyVolunteeringFacade();
    protected abstract OrganizationsFacade createOrganizationsFacade();
    protected abstract OrganizationsFacade createSpyOrganizationsFacade();
    protected abstract ReportsFacade createReportsFacade();
    protected abstract ReportsFacade createSpyReportsFacade();
    protected abstract NotificationSystem createNotificationSystem();

    private int organizationId, volunteeringId1, volunteeringId2, volunteeringPostId, volunteerPostId;
    private final String title = "Postito";
    private final String description = "Postito is a very cool post";
    private final String posterUsername = "Poster";
    private final String otherManagerUsername = "Manager";
    private final String nonPosterUsername = "NonPoster";
    private final String admin = "Admin";
    private final String newUser = "NewUser";

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setup() {
        postsFacade = createPostsFacade();
        usersFacade = createUsersFacade();
        organizationsFacade = createOrganizationsFacade();
        spyOrganizationsFacade = createSpyOrganizationsFacade();
        volunteeringFacade = createVolunteeringFacade();
        spyVolunteeringFacade = createSpyVolunteeringFacade();
        reportsFacade = createReportsFacade();
        spyReportsFacade = createSpyReportsFacade();
        notificationSystem = createNotificationSystem();
        notificationSystem.setSender(sender);

        createPostsFacade().getKeywordExtractor().setAI(this.ai);
        createPostsFacade().getSkillsAndCategoriesExtractor().setAI(this.ai);

        if (!Mockito.mockingDetails(spyKeywordExtractor).isSpy()) {
            spyKeywordExtractor = Mockito.spy(createPostsFacade().getKeywordExtractor());
        }
        if (!Mockito.mockingDetails(spySkillsAndCategoriesExtractor).isSpy()) {
            spySkillsAndCategoriesExtractor = spy(createPostsFacade().getSkillsAndCategoriesExtractor());
        }

        postsFacade.setReportsFacade(spyReportsFacade);
        postsFacade.setOrganizationsFacade(spyOrganizationsFacade);
        postsFacade.setKeywordExtractor(spyKeywordExtractor);
        postsFacade.setSkillsAndCategoriesExtractor(spySkillsAndCategoriesExtractor);
        postsFacade.setVolunteeringFacade(spyVolunteeringFacade);

        applicationContext.getBean(UserJPA.class).deleteAll();
        applicationContext.getBean(VolunteeringJPA.class).deleteAll();
        applicationContext.getBean(OrganizationJPA.class).deleteAll();
        applicationContext.getBean(VolunteeringPostJPA.class).deleteAll();
        applicationContext.getBean(VolunteerPostJPA.class).deleteAll();
        applicationContext.getBean(RequestJPA.class).deleteAll();
        applicationContext.getBean(ReportJPA.class).deleteAll();
        applicationContext.getBean(BannedJPA.class).deleteAll();
        applicationContext.getBean(HourRequestJPA.class).deleteAll();
        applicationContext.getBean(AppointmentJPA.class).deleteAll();
        applicationContext.getBean(MessageJPA.class).deleteAll();
        applicationContext.getBean(NotificationJPA.class).deleteAll();

        usersFacade.register(posterUsername, "password", "Moshe Cohen", "moshe@gmail.com", "0541967544", new Date());
        usersFacade.register(otherManagerUsername, "password", "Yossi Levi", "yossi@gmail.com", "0541967544", new Date());
        usersFacade.register(nonPosterUsername, "password", "Miriam Cohen", "miriam@gmail.com", "0541967544", new Date());
        usersFacade.registerAdmin(admin, "password", "Admin Cohen", "moshe@gmail.com", "0541967544", new Date());
        usersFacade.updateUserSkills(posterUsername, List.of("keyword1"));
        usersFacade.updateUserSkills(otherManagerUsername, List.of("keyword2"));
        usersFacade.updateUserSkills(nonPosterUsername, List.of("keyword3"));
        usersFacade.updateUserSkills(admin, List.of("keyword5"));
        this.organizationId = organizationsFacade.createOrganization("Organization", "This is a very cool organization.", "0541967544", "org@gmail.com", posterUsername);
        organizationsFacade.sendAssignManagerRequest(otherManagerUsername, posterUsername, organizationId);
        organizationsFacade.handleAssignManagerRequest(otherManagerUsername, organizationId, true);
        this.volunteeringId1 = organizationsFacade.createVolunteering(organizationId, "Volunteering1", "This is a very cool volunteering.", posterUsername);
        this.volunteeringId2 = organizationsFacade.createVolunteering(organizationId, "Volunteering2", "This is a very cool volunteering.", posterUsername);
        this.volunteeringPostId = postsFacade.createVolunteeringPost(title, description, posterUsername, volunteeringId1);

        this.volunteerPostId = postsFacade.createVolunteerPost(title, description, posterUsername);
        postsFacade.sendAddRelatedUserRequest(volunteerPostId, otherManagerUsername, posterUsername);
        postsFacade.handleAddRelatedUserRequest(volunteerPostId, otherManagerUsername, true);

        reset(sender);
        reset(spyOrganizationsFacade);
        reset(spyReportsFacade);
        reset(spyKeywordExtractor);
        reset(spySkillsAndCategoriesExtractor);
    }

    @AfterEach
    void tearDown() {

    }

    private void verifyMessage(String message, List<String> usersnames) {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(sender).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        verify(sender, times(usersnames.size())).sendNotification(recipientCaptor.capture(), captor.capture());

        List<String> recipients = recipientCaptor.getAllValues();
        List<Notification> notifications = captor.getAllValues();

        assertTrue(recipients.containsAll(usersnames));
        for (Notification notification : notifications) {
            assertEquals(message, notification.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("validVolunteeringPostInputs")
    void givenValidFields_whenCreateVolunteeringPost_thenCreate(String title, String description, String actor) {
        when(ai.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");

        int numPostsBefore = postsFacade.getAllVolunteeringPosts(posterUsername).size();

        postsFacade.createVolunteeringPost(title, description, actor, volunteeringId1);
        verify(spyKeywordExtractor).getVolunteeringPostKeywords(eq("Volunteering1"), eq("This is a very cool volunteering."), eq(title), eq(description));

        String newPostMessage = String.format("The new post \"%s\" might be relevant for you!", title);
        String managersMessage = String.format("The new post \"%s\" was created for the volunteering \"%s\" in your organization \"%s\".", title, "Volunteering1", "Organization");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(sender).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        verify(sender, times(5)).sendNotification(recipientCaptor.capture(), captor.capture());

        List<String> recipients = recipientCaptor.getAllValues();
        List<Notification> notifications = captor.getAllValues();

        assertTrue(recipients.subList(0, 3).containsAll(List.of(posterUsername, otherManagerUsername, nonPosterUsername)));
        for (Notification notification : notifications.subList(0, 3)) {
            assertEquals(newPostMessage, notification.getMessage());
        }

        assertTrue(recipients.subList(3, 5).containsAll(List.of(posterUsername, otherManagerUsername)));
        for (Notification notification : notifications.subList(3, 5)) {
            assertEquals(managersMessage, notification.getMessage());
        }

        int numPostsAfter = postsFacade.getAllVolunteeringPosts(posterUsername).size();
        assertEquals(1, numPostsAfter - numPostsBefore);
    }

    @Test
    void givenNonExistingUser_whenCreateVolunteeringPost_thenThrowException() {
        int numPostsBefore = postsFacade.getAllVolunteeringPosts(posterUsername).size();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.createVolunteeringPost(title, description, newUser, volunteeringId1);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        verify(spyKeywordExtractor, times(0)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        int numPostsAfter = postsFacade.getAllVolunteeringPosts(posterUsername).size();
        assertEquals(0, numPostsAfter - numPostsBefore);
    }

    @Test
    void givenNonManagerOrAdmin_whenCreateVolunteeringPost_thenThrowException() {
        int numPostsBefore = postsFacade.getAllVolunteeringPosts(posterUsername).size();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.createVolunteeringPost(title, description, nonPosterUsername, volunteeringId1);
        });
        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(nonPosterUsername, "Organization", "post about the organization's volunteering"), exception.getMessage());

        verify(spyKeywordExtractor, times(0)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        int numPostsAfter = postsFacade.getAllVolunteeringPosts(posterUsername).size();
        assertEquals(0, numPostsAfter - numPostsBefore);
    }

    @Test
    void givenInvalidFields_whenCreateVolunteeringPost_thenThrowException() {
        int numPostsBefore = postsFacade.getAllVolunteeringPosts(posterUsername).size();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.createVolunteeringPost("", description, posterUsername, volunteeringId1);
        });
        assertEquals("Invalid post title: .\n", exception.getMessage());

        verify(spyKeywordExtractor, times(1)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        int numPostsAfter = postsFacade.getAllVolunteeringPosts(posterUsername).size();
        assertEquals(0, numPostsAfter - numPostsBefore);
    }

    @Test
    void givenNonExistingVolunteeringId_whenCreateVolunteeringPost_thenThrowException() {
        int numPostsBefore = postsFacade.getAllVolunteeringPosts(posterUsername).size();

        int newVolunteeringId = Math.max(volunteeringId1, volunteeringId2) + 1;
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.createVolunteeringPost(title, description, posterUsername, newVolunteeringId);
        });
        assertEquals("Volunteering with id " + newVolunteeringId + " does not exist", exception.getMessage());

        verify(spyKeywordExtractor, times(0)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        int numPostsAfter = postsFacade.getAllVolunteeringPosts(posterUsername).size();
        assertEquals(0, numPostsAfter - numPostsBefore);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Poster", "Admin", "Manager"})
    void givenExistingPostByManagerOrAdmin_whenRemoveVolunteeringPost_thenRemove(String username) {
        assertDoesNotThrow(() -> postsFacade.getVolunteeringPost(volunteeringPostId, username));

        postsFacade.removeVolunteeringPost(volunteeringPostId, username);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getVolunteeringPost(volunteeringPostId, username);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteeringPostId), exception.getMessage());

        verify(spyReportsFacade).removeVolunteeringPostReports(volunteeringPostId);

        String message = String.format("The post \"%s\" of the volunteering \"%s\" in your organization \"%s\" was removed", title, "Volunteering1", "Organization");
        verifyMessage(message, List.of(posterUsername, otherManagerUsername));
    }

    @Test
    void givenNonExistingUser_whenRemoveVolunteeringPost_thenThrowException() {
        assertDoesNotThrow(() -> postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeVolunteeringPost(volunteeringPostId, newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        assertDoesNotThrow(() -> postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername));
        verifyNoInteractions(spyReportsFacade);
        verify(spyOrganizationsFacade, times(0)).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void givenNonExistingPost_whenRemoveVolunteeringPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeVolunteeringPost(volunteeringPostId + 1, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteeringPostId + 1), exception.getMessage());
        verifyNoInteractions(spyReportsFacade);
        verify(spyOrganizationsFacade, times(0)).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void givenNonPoster_whenRemoveVolunteeringPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeVolunteeringPost(volunteeringPostId, nonPosterUsername);
        });
        assertEquals(PostErrors.makeUserIsNotAllowedToMakePostActionError(title, nonPosterUsername, "remove"), exception.getMessage());
        verifyNoInteractions(spyReportsFacade);
        verify(spyOrganizationsFacade, times(0)).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void givenVolunteeringId_whenRemovePostsByVolunteeringId_thenRemove() {
        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);

        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);

        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);

        List<VolunteeringPostDTO> expectedBefore = List.of(post1, post2, post3);
        List<VolunteeringPostDTO> allPostsBefore = postsFacade.getAllVolunteeringPosts(posterUsername);
        assertEquals(new HashSet<>(expectedBefore), new HashSet<>(allPostsBefore));

        postsFacade.removePostsByVolunteeringId(volunteeringId1);

        List<VolunteeringPostDTO> expectedAfter = List.of(post3);
        List<VolunteeringPostDTO> allPostsAfter = postsFacade.getAllVolunteeringPosts(posterUsername);
        assertEquals(new HashSet<>(expectedAfter), new HashSet<>(allPostsAfter));

        postsFacade.removePostsByVolunteeringId(volunteeringId2);

        expectedAfter = new ArrayList<>();
        allPostsAfter = postsFacade.getAllVolunteeringPosts(posterUsername);
        assertEquals(new HashSet<>(expectedAfter), new HashSet<>(allPostsAfter));
    }

    private boolean verifyPostFields(PostDTO post, String title, String description) {
        return post.getTitle().equals(title) &&
                post.getDescription().equals(description);
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

    private static Stream<Arguments> validVolunteerPostInputs() {
        return Stream.of(
                Arguments.of("Help with Animals", "We need volunteers to care for rescued dogs.", "Poster"),
                Arguments.of("Food Drive", "Join us in organizing a local food drive event.", "Poster"),
                Arguments.of("Beach Cleanup", "Help clean up the beach and protect marine life.", "Poster"),
                Arguments.of("Tutoring Kids", "Volunteer to tutor kids in math and reading.", "Poster"),
                Arguments.of("Art Therapy", "Support art therapy workshops for mental health.", "Poster")
        );
    }

    @ParameterizedTest
    @MethodSource("validVolunteeringPostInputs")
    void givenExistingPostAndValidFieldsByManagerOrAdmin_whenEditVolunteeringPost_thenEdit(String newTitle, String newDescription, String actor) {
        VolunteeringPostDTO post = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));

        assertDoesNotThrow(() -> postsFacade.editVolunteeringPost(volunteeringPostId, newTitle, newDescription, actor));

        post = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        assertTrue(verifyPostFields(post, newTitle, newDescription));

        verify(spyKeywordExtractor, times(1)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        String message = String.format("The post \"%s\" of the volunteering \"%s\" in your organization \"%s\" was edited.", title, "Volunteering1", "Organization");
        verifyMessage(message, List.of(posterUsername, otherManagerUsername));
    }

    @Test
    void givenNonExistingUser_whenEditVolunteeringPost_thenThrowException() {
        VolunteeringPostDTO post = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.editVolunteeringPost(volunteeringPostId, "title", "description", newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        post = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));

        verify(spyKeywordExtractor, times(0)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(spyOrganizationsFacade, times(0)).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void givenNonValidFields_whenEditVolunteeringPost_thenThrowException() {
        VolunteeringPostDTO post = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));

        assertThrows(IllegalArgumentException.class, () -> postsFacade.editVolunteeringPost(volunteeringPostId, "", "", posterUsername));

        post = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));

        verify(spyKeywordExtractor, times(1)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(spyOrganizationsFacade, times(0)).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void givenNonExistingPost_whenEditVolunteeringPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.editVolunteeringPost(volunteeringPostId + 1, "title", "description", posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteeringPostId + 1), exception.getMessage());

        verify(spyKeywordExtractor, times(0)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(spyOrganizationsFacade, times(0)).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void givenNonManager_whenEditVolunteeringPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.editVolunteeringPost(volunteeringPostId, "title", "description", nonPosterUsername);
        });
        assertEquals(PostErrors.makeUserIsNotAllowedToMakePostActionError(title, nonPosterUsername, "edit"), exception.getMessage());

        verify(spyKeywordExtractor, times(0)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(spyOrganizationsFacade, times(0)).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void givenVolunteeringId_whenUpdateVolunteeringPostsKeywords_thenUpdate() {
        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);
        VolunteeringPostDTO post3Before = postsFacade.getVolunteeringPost(postId3, posterUsername);

        when(ai.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");
        reset(spyKeywordExtractor);

        postsFacade.updateVolunteeringPostsKeywords(volunteeringId1, posterUsername);
        verify(spyKeywordExtractor, times(1)).getVolunteeringPostKeywords(eq("Volunteering1"), eq("This is a very cool volunteering."), eq(title), eq(description));
        verify(spyKeywordExtractor, times(1)).getVolunteeringPostKeywords(eq("Volunteering1"), eq("This is a very cool volunteering."), eq("Title2"), eq("Description2"));
        verify(spyKeywordExtractor, times(0)).getVolunteeringPostKeywords(eq("Volunteering2"), eq("This is a very cool volunteering."), eq("Title3"), eq("Description3"));

        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);

        Set<String> newKeywords = Set.of("keyword1", "keyword2", "keyword3", "keyword4");
        assertEquals(newKeywords, post1.getKeywords());
        assertEquals(newKeywords, post2.getKeywords());
        assertEquals(post3Before.getKeywords(), post3.getKeywords());
    }

    @Test
    void givenNonExistingVolunteeringId_whenUpdateVolunteeringPostsKeywords_thenDoNotUpdate() {
        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);

        VolunteeringPostDTO post1Before = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        VolunteeringPostDTO post2Before = postsFacade.getVolunteeringPost(postId2, posterUsername);
        VolunteeringPostDTO post3Before = postsFacade.getVolunteeringPost(postId3, posterUsername);

        when(ai.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");
        reset(spyKeywordExtractor);

        postsFacade.updateVolunteeringPostsKeywords(Math.max(volunteeringId1, volunteeringId2) + 1, posterUsername);
        verify(spyKeywordExtractor, times(0)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);

        assertEquals(post1Before.getKeywords(), post1.getKeywords());
        assertEquals(post2Before.getKeywords(), post2.getKeywords());
        assertEquals(post3Before.getKeywords(), post3.getKeywords());
    }

    @Test
    void givenNonExistingUser_whenUpdateVolunteeringPostsKeywords_thenThrowException() {
        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);

        VolunteeringPostDTO post1Before = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        VolunteeringPostDTO post2Before = postsFacade.getVolunteeringPost(postId2, posterUsername);
        VolunteeringPostDTO post3Before = postsFacade.getVolunteeringPost(postId3, posterUsername);

        when(ai.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");
        reset(spyKeywordExtractor);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.updateVolunteeringPostsKeywords(volunteeringId1, newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        verify(spyKeywordExtractor, times(0)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);

        assertEquals(post1Before.getKeywords(), post1.getKeywords());
        assertEquals(post2Before.getKeywords(), post2.getKeywords());
        assertEquals(post3Before.getKeywords(), post3.getKeywords());
    }

    @Test
    void doesVolunteeringPostExist() {
        assertTrue(postsFacade.doesVolunteeringPostExist(volunteeringPostId));
        assertFalse(postsFacade.doesVolunteeringPostExist(volunteeringPostId + 1));
    }

    @Test
    void doesVolunteerPostExist() {
        assertTrue(postsFacade.doesVolunteerPostExist(volunteerPostId));
        assertFalse(postsFacade.doesVolunteerPostExist(volunteerPostId + 1));
    }

    @Test
    void givenExistingUserAndPost_whenGetVolunteeringPost_thenReturnPost() {
        VolunteeringPostDTO post = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));
    }

    @Test
    void givenNonExistingUser_whenGetVolunteeringPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getVolunteeringPost(volunteeringPostId, newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());
    }

    @Test
    void givenNonExistingPost_whenGetVolunteeringPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getVolunteeringPost(volunteeringPostId + 1, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteeringPostId + 1), exception.getMessage());
    }

    @Test
    void givenExistingUserAndPost_whenGetVolunteerPost_thenReturnPost() {
        VolunteerPostDTO post = postsFacade.getVolunteerPost(volunteerPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));
    }

    @Test
    void givenNonExistingUser_whenGetVolunteerPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getVolunteerPost(volunteerPostId, newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());
    }

    @Test
    void givenNonExistingPost_whenGetVolunteerPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getVolunteerPost(volunteerPostId + 1, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId + 1), exception.getMessage());
    }

    @Test
    void givenExistingUser_whenGetAllVolunteeringPosts_thenReturnPosts() {
        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);

        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);

        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);

        List<VolunteeringPostDTO> expected = List.of(post1, post2, post3);
        List<VolunteeringPostDTO> res = postsFacade.getAllVolunteeringPosts(posterUsername);
        assertEquals(new HashSet<>(expected), new HashSet<>(res));
    }

    @Test
    void givenNonExistingUser_whenGetAllVolunteeringPosts_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getAllVolunteeringPosts(newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());
    }

    @Test
    void givenExistingUserAndOrganization_whenGetOrganizationVolunteeringPosts_thenReturnPosts() {
        int orgId2 = organizationsFacade.createOrganization("Org2", "Desc", "0541980766", "org@gmail.com", posterUsername);
        int volunteeringId3 = organizationsFacade.createVolunteering(orgId2, "Volunteering3", "This is a very cool volunteering.", posterUsername);

        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);

        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);

        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);

        int postId4 = postsFacade.createVolunteeringPost("Title4", "Description4", posterUsername, volunteeringId3);
        VolunteeringPostDTO post4 = postsFacade.getVolunteeringPost(postId4, posterUsername);

        int postId5 = postsFacade.createVolunteeringPost("Title5", "Description5", posterUsername, volunteeringId3);
        VolunteeringPostDTO post5 = postsFacade.getVolunteeringPost(postId5, posterUsername);

        List<VolunteeringPostDTO> expectedOrg1 = List.of(post1, post2, post3);
        List<VolunteeringPostDTO> expectedOrg2 = List.of(post4, post5);

        List<VolunteeringPostDTO> resOrg1 = postsFacade.getOrganizationVolunteeringPosts(organizationId, posterUsername);
        List<VolunteeringPostDTO> resOrg2 = postsFacade.getOrganizationVolunteeringPosts(orgId2, posterUsername);

        assertEquals(new HashSet<>(expectedOrg1), new HashSet<>(resOrg1));
        assertEquals(new HashSet<>(expectedOrg2), new HashSet<>(resOrg2));
    }

    @Test
    void givenNonExistingUser_whenGetOrganizationVolunteeringPosts_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getOrganizationVolunteeringPosts(organizationId, newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());
    }

    @Test
    void givenNonExistingOrganization_whenGetOrganizationVolunteeringPosts_thenReturnEmptyList() {
        List<VolunteeringPostDTO> posts = postsFacade.getOrganizationVolunteeringPosts(organizationId + 1, posterUsername);
        assertEquals(0, posts.size());
    }

    private boolean joinVolunteeringRequestExists(String manager, int volunteeringId, String joiner) {
        try {
            volunteeringFacade.denyUserJoinRequest(manager, volunteeringId, joiner);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Test
    void givenNonVolunteerOrManagerAndExistingPost_whenJoinVolunteeringRequest_thenAddRequest() {
        String freeText = "I want to join.";
        VolunteeringPostDTO postBefore = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        int numPeopleBefore = postBefore.getNumOfPeopleRequestedToJoin();
        int volunteeringId = postBefore.getVolunteeringId();

        assertFalse(joinVolunteeringRequestExists(posterUsername, volunteeringId, nonPosterUsername));

        postsFacade.joinVolunteeringRequest(volunteeringPostId, nonPosterUsername, freeText);

        VolunteeringPostDTO postAfter = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        int numPeopleAfter = postAfter.getNumOfPeopleRequestedToJoin();
        assertEquals(1, numPeopleAfter - numPeopleBefore);

        String message = nonPosterUsername + " has requested to join volunteering Volunteering1";
        verifyMessage(message, List.of(posterUsername, otherManagerUsername));

        verify(spyVolunteeringFacade).requestToJoinVolunteering(nonPosterUsername, volunteeringId, freeText);
        assertTrue(joinVolunteeringRequestExists(posterUsername, volunteeringId, nonPosterUsername));
    }

    @Test
    void givenNonExistingUser_whenJoinVolunteeringRequest_thenThrowException() {
        String freeText = "I want to join.";
        VolunteeringPostDTO postBefore = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        int numPeopleBefore = postBefore.getNumOfPeopleRequestedToJoin();
        int volunteeringId = postBefore.getVolunteeringId();

        assertFalse(joinVolunteeringRequestExists(posterUsername, volunteeringId, nonPosterUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.joinVolunteeringRequest(volunteeringPostId, newUser, freeText);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        VolunteeringPostDTO postAfter = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        int numPeopleAfter = postAfter.getNumOfPeopleRequestedToJoin();
        assertEquals(0, numPeopleAfter - numPeopleBefore);

        verify(spyOrganizationsFacade, times(0)).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());

        verify(spyVolunteeringFacade, times(0)).requestToJoinVolunteering(newUser, volunteeringId, freeText);
        assertFalse(joinVolunteeringRequestExists(posterUsername, volunteeringId, newUser));
    }

    @Test
    void givenNonExistingPost_whenJoinVolunteeringRequest_thenThrowException() {
        String freeText = "I want to join.";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.joinVolunteeringRequest(volunteeringPostId + 1, nonPosterUsername, freeText);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteeringPostId + 1), exception.getMessage());

        verify(spyOrganizationsFacade, times(0)).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
        verify(spyVolunteeringFacade, times(0)).requestToJoinVolunteering(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    void givenManager_whenJoinVolunteeringRequest_thenThrowException() {
        String freeText = "I want to join.";
        VolunteeringPostDTO postBefore = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        int numPeopleBefore = postBefore.getNumOfPeopleRequestedToJoin();
        int volunteeringId = postBefore.getVolunteeringId();

        assertFalse(joinVolunteeringRequestExists(posterUsername, volunteeringId, otherManagerUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.joinVolunteeringRequest(volunteeringPostId, otherManagerUsername, freeText);
        });
        assertEquals("User " + otherManagerUsername + " is already a manager in organization of volunteering " + volunteeringId, exception.getMessage());

        VolunteeringPostDTO postAfter = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        int numPeopleAfter = postAfter.getNumOfPeopleRequestedToJoin();
        assertEquals(0, numPeopleAfter - numPeopleBefore);

        verify(spyOrganizationsFacade, times(0)).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());

        verify(spyVolunteeringFacade, times(1)).requestToJoinVolunteering(otherManagerUsername, volunteeringId, freeText);
        assertFalse(joinVolunteeringRequestExists(posterUsername, volunteeringId, otherManagerUsername));
    }

    @Test
    void givenVolunteer_whenJoinVolunteeringRequest_thenThrowException() {
        String freeText = "I want to join.";

        postsFacade.joinVolunteeringRequest(volunteeringPostId, nonPosterUsername, freeText);
        reset(spyVolunteeringFacade);

        VolunteeringPostDTO postBefore = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        int numPeopleBefore = postBefore.getNumOfPeopleRequestedToJoin();
        int volunteeringId = postBefore.getVolunteeringId();

        volunteeringFacade.acceptUserJoinRequest(posterUsername, volunteeringId, nonPosterUsername, 0);

        assertFalse(joinVolunteeringRequestExists(posterUsername, volunteeringId, nonPosterUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.joinVolunteeringRequest(volunteeringPostId, nonPosterUsername, freeText);
        });
        assertEquals("User " + nonPosterUsername + " is already a volunteer in volunteering " + volunteeringId, exception.getMessage());

        VolunteeringPostDTO postAfter = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        int numPeopleAfter = postAfter.getNumOfPeopleRequestedToJoin();
        assertEquals(0, numPeopleAfter - numPeopleBefore);

        verify(spyOrganizationsFacade, times(0)).notifyManagers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());

        verify(spyVolunteeringFacade, times(1)).requestToJoinVolunteering(nonPosterUsername, volunteeringId, freeText);
        assertFalse(joinVolunteeringRequestExists(posterUsername, volunteeringId, nonPosterUsername));
    }

    @Test
    void sortByRelevance() {
        usersFacade.updateUserPreferences(nonPosterUsername, List.of("Animals"));
        usersFacade.updateUserSkills(nonPosterUsername, List.of("Driving", "Baking"));

        int volunteeringId3 = organizationsFacade.createVolunteering(organizationId, "Baking", "Driving animals while baking.", posterUsername);
        volunteeringFacade.updateVolunteeringSkills(posterUsername, volunteeringId3, List.of("BakiNg", "driving"));
        volunteeringFacade.updateVolunteeringCategories(posterUsername, volunteeringId3, List.of("Animals"));

        int post1Id = postsFacade.createVolunteeringPost("Plants", "Gardening", posterUsername, volunteeringId1);
        int post2Id = postsFacade.createVolunteeringPost("Animals", "Driving", posterUsername, volunteeringId1);
        int post3Id = postsFacade.createVolunteeringPost("Cars", "Driving", posterUsername, volunteeringId1);
        int post4Id = postsFacade.createVolunteeringPost("Idk", "Idk", posterUsername, volunteeringId3);

        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(post1Id, posterUsername);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(post2Id, posterUsername);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(post3Id, posterUsername);
        VolunteeringPostDTO post4 = postsFacade.getVolunteeringPost(post4Id, posterUsername);

        List<VolunteeringPostDTO> allPosts = List.of(post1, post2, post3, post4);

        List<VolunteeringPostDTO> res = postsFacade.sortByRelevance(nonPosterUsername, allPosts);
        assertEquals(4, res.size());
        assertEquals(post4, res.get(0));
        assertEquals(post2, res.get(1));
        assertEquals(post3, res.get(2));
        assertEquals(post1, res.get(3));
    }

    private static Stream<Arguments> numJoiners() {
        return Stream.of(
                Arguments.of(1, 2, 3),
                Arguments.of(2, 1, 3),
                Arguments.of(1, 3, 2),
                Arguments.of(2, 3, 1),
                Arguments.of(3, 1, 2),
                Arguments.of(3, 2, 1)
        );
    }

    private void setPopularity(int popularity, int postId, int initId) {
        for(int i = 0; i < popularity; i++) {
            int userNum = i + initId;
            usersFacade.register("user" + userNum, "password", "user" + userNum, "user" + userNum + "@gmail.com", "0541967544", new Date());
            postsFacade.joinVolunteeringRequest(postId, "user" + userNum, "i want to join.");
        }
    }

    @ParameterizedTest
    @MethodSource("numJoiners")
    void givenPosts_whenSortByPopularity_thenSort(int post1Popularity, int post2Popularity, int post3Popularity) {
        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);

        setPopularity(post1Popularity, volunteeringPostId, 0);
        setPopularity(post2Popularity, postId2, post1Popularity);
        setPopularity(post3Popularity, postId3, post1Popularity + post2Popularity);

        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);
        List<VolunteeringPostDTO> allPosts = List.of(post1, post2, post3);

        VolunteeringPostDTO[] expected = new VolunteeringPostDTO[3];
        expected[3 - post1Popularity] = post1;
        expected[3 - post2Popularity] = post2;
        expected[3 - post3Popularity] = post3;

        List<VolunteeringPostDTO> res = postsFacade.sortByPopularity(posterUsername, allPosts);

        assertEquals(Arrays.asList(expected), res);
    }

    @Test
    void givenNonExistingUser_whenSortByPopularity_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.sortByPopularity(newUser, new ArrayList<>());
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());
    }

    @Test
    void filterVolunteeringPosts() {
        volunteeringFacade.updateVolunteeringSkills(posterUsername, volunteeringId1, List.of("skill1"));
        volunteeringFacade.updateVolunteeringSkills(posterUsername, volunteeringId2, List.of("skill2"));
        volunteeringFacade.addVolunteeringLocation(posterUsername, volunteeringId1, "loc1", new AddressTuple("Beer Sheva", "", ""));
        volunteeringFacade.addVolunteeringLocation(posterUsername, volunteeringId2, "loc2", new AddressTuple("Tel Aviv", "", ""));

        int postId1 = postsFacade.createVolunteeringPost("Animal Shelter Help", "Looking for volunteers to help feed and care for dogs and cats.", posterUsername, volunteeringId1);
        int postId2 = postsFacade.createVolunteeringPost("Community Garden Project", "Join us in planting and maintaining a local community garden.", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Elderly Tech Support", "Assist elderly community members with basic smartphone and computer skills.", posterUsername, volunteeringId1);
        int postId4 = postsFacade.createVolunteeringPost("Beach Cleanup", "Help clean up our local beach and protect marine life.", posterUsername, volunteeringId2);
        int postId5 = postsFacade.createVolunteeringPost("Library Reading Buddy", "Read books to kids at the local library every weekend.", posterUsername, volunteeringId2);
        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(postId1, posterUsername);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);
        VolunteeringPostDTO post4 = postsFacade.getVolunteeringPost(postId4, posterUsername);
        VolunteeringPostDTO post5 = postsFacade.getVolunteeringPost(postId5, posterUsername);
        List<Integer> allPosts = List.of(postId1, postId2, postId3, postId4, postId5);

        List<VolunteeringPostDTO> res = postsFacade.filterVolunteeringPosts(Set.of(), Set.of("skill1"), Set.of(), Set.of(), Set.of(), posterUsername, allPosts, false);
        List<VolunteeringPostDTO> expected = List.of(post1, post2, post3);
        assertEquals(expected, res);

        res = postsFacade.filterVolunteeringPosts(Set.of(), Set.of(), Set.of(), Set.of("Organization"), Set.of("Volunteering2"), posterUsername, allPosts, false);
        expected = List.of(post4, post5);
        assertEquals(expected, res);

        res = postsFacade.filterVolunteeringPosts(Set.of(), Set.of(), Set.of("Tel Aviv", "Beer Sheva"), Set.of(), Set.of(), posterUsername, allPosts, false);
        expected = List.of(post1, post2, post3, post4, post5);
        assertEquals(expected, res);

        res = postsFacade.filterVolunteeringPosts(Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), posterUsername, allPosts, false);
        expected = List.of(post1, post2, post3, post4, post5);
        assertEquals(expected, res);

        res = postsFacade.filterVolunteeringPosts(Set.of("NewSkill"), Set.of(), Set.of(), Set.of(), Set.of(), posterUsername, allPosts, false);
        expected = List.of();
        assertEquals(expected, res);
    }

    @Test
    void filterVolunteerPosts() {
        setupVolunteerPostMock();

        int postId1 = postsFacade.createVolunteerPost("Animal Shelter Help", "Looking for volunteers to help feed and care for dogs and cats.", posterUsername);
        int postId2 = postsFacade.createVolunteerPost("Community Garden Project", "Join us in planting and maintaining a local community garden.", posterUsername);
        int postId3 = postsFacade.createVolunteerPost("Elderly Tech Support", "Assist elderly community members with basic smartphone and computer skills.", posterUsername);
        int postId4 = postsFacade.createVolunteerPost("Beach Cleanup", "Help clean up our local beach and protect marine life.", posterUsername);
        int postId5 = postsFacade.createVolunteerPost("Library Reading Buddy", "Read books to kids at the local library every weekend.", nonPosterUsername);
        VolunteerPostDTO post1 = postsFacade.getVolunteerPost(postId1, posterUsername);
        VolunteerPostDTO post2 = postsFacade.getVolunteerPost(postId2, posterUsername);
        VolunteerPostDTO post3 = postsFacade.getVolunteerPost(postId3, posterUsername);
        VolunteerPostDTO post4 = postsFacade.getVolunteerPost(postId4, posterUsername);
        VolunteerPostDTO post5 = postsFacade.getVolunteerPost(postId5, nonPosterUsername);
        List<Integer> allPosts = List.of(postId1, postId2, postId3, postId4, postId5);

        List<PostDTO> expectedCommunityAndNature = List.of(post2, post3, post4, post5);
        List<PostDTO> expectedTechnology = List.of(post3);
        List<PostDTO> expectedReading = List.of(post5);
        List<PostDTO> expectedAll = List.of(post1, post2, post3, post4, post5);
        List<PostDTO> expectedNone = List.of();
        List<PostDTO> expectedMine = List.of(post5);

        List<VolunteerPostDTO> resCommunityAndNature = postsFacade.filterVolunteerPosts(Set.of("community", "nature"), Set.of(), posterUsername, allPosts, false);
        List<VolunteerPostDTO> resTechnology = postsFacade.filterVolunteerPosts(Set.of("Technology"), Set.of(), posterUsername, allPosts, false);
        List<VolunteerPostDTO> resReading = postsFacade.filterVolunteerPosts(Set.of(), Set.of("Reading"), posterUsername, allPosts, false);
        List<VolunteerPostDTO> resAll = postsFacade.filterVolunteerPosts(Set.of(), Set.of(), posterUsername, allPosts, false);
        List<VolunteerPostDTO> resNone = postsFacade.filterVolunteerPosts(Set.of("None"), Set.of(), posterUsername, allPosts, false);
        List<VolunteerPostDTO> resMine = postsFacade.filterVolunteerPosts(Set.of(), Set.of(), nonPosterUsername, allPosts, true);

        assertEquals(expectedCommunityAndNature, resCommunityAndNature);
        assertEquals(expectedTechnology, resTechnology);
        assertEquals(expectedReading, resReading);
        assertEquals(expectedAll, resAll);
        assertEquals(expectedNone, resNone);
        assertEquals(expectedMine, resMine);
    }

    @Test
    void getAllPostsCategories() {
        volunteeringFacade.updateVolunteeringCategories(posterUsername, volunteeringId1, List.of("cat1", "cat2", "cat3"));
        volunteeringFacade.updateVolunteeringCategories(posterUsername, volunteeringId2, List.of("cat4", "cat5", "cat6"));

        List<String> expectedVol1 = List.of("cat1", "cat2", "cat3");
        List<String> resOnePost = postsFacade.getAllPostsCategories();
        assertEquals(new HashSet<>(expectedVol1), new HashSet<>(resOnePost));

        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);

        List<String> expectedAll = List.of("cat1", "cat2", "cat3", "cat4", "cat5", "cat6");
        List<String> resAll = postsFacade.getAllPostsCategories();
        assertEquals(new HashSet<>(expectedAll), new HashSet<>(resAll));
    }

    @Test
    void getAllPostsSkills() {
        volunteeringFacade.updateVolunteeringSkills(posterUsername, volunteeringId1, List.of("skill1", "skill2", "skill3"));
        volunteeringFacade.updateVolunteeringSkills(posterUsername, volunteeringId2, List.of("skill4", "skill5", "skill6"));

        List<String> expectedVol1 = List.of("skill1", "skill2", "skill3");
        List<String> resOnePost = postsFacade.getAllPostsSkills();
        assertEquals(new HashSet<>(expectedVol1), new HashSet<>(resOnePost));

        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);

        List<String> expectedAll = List.of("skill1", "skill2", "skill3", "skill4", "skill5", "skill6");
        List<String> resAll = postsFacade.getAllPostsSkills();
        assertEquals(new HashSet<>(expectedAll), new HashSet<>(resAll));
    }

    @Test
    void getAllVolunteerPostsCategories() {
        setupVolunteerPostMock();

        int postId1 = postsFacade.createVolunteerPost("Animal Shelter Help", "Looking for volunteers to help feed and care for dogs and cats.", posterUsername);
        int postId2 = postsFacade.createVolunteerPost("Community Garden Project", "Join us in planting and maintaining a local community garden.", posterUsername);
        int postId3 = postsFacade.createVolunteerPost("Elderly Tech Support", "Assist elderly community members with basic smartphone and computer skills.", posterUsername);
        int postId4 = postsFacade.createVolunteerPost("Beach Cleanup", "Help clean up our local beach and protect marine life.", posterUsername);
        int postId5 = postsFacade.createVolunteerPost("Library Reading Buddy", "Read books to kids at the local library every weekend.", nonPosterUsername);

        List<String> expected = List.of("Animals", "Welfare", "Environment", "Community", "Education", "Community", "Environment", "Public Health", "Education", "Youth");
        assertEquals(new HashSet<>(expected), new HashSet<>(postsFacade.getAllVolunteerPostsCategories()));
    }

    @Test
    void getAllVolunteerPostsSkills() {
        setupVolunteerPostMock();

        int postId1 = postsFacade.createVolunteerPost("Animal Shelter Help", "Looking for volunteers to help feed and care for dogs and cats.", posterUsername);
        int postId2 = postsFacade.createVolunteerPost("Community Garden Project", "Join us in planting and maintaining a local community garden.", posterUsername);
        int postId3 = postsFacade.createVolunteerPost("Elderly Tech Support", "Assist elderly community members with basic smartphone and computer skills.", posterUsername);
        int postId4 = postsFacade.createVolunteerPost("Beach Cleanup", "Help clean up our local beach and protect marine life.", posterUsername);
        int postId5 = postsFacade.createVolunteerPost("Library Reading Buddy", "Read books to kids at the local library every weekend.", nonPosterUsername);

        List<String> expected = List.of("animal care", "empathy", "cleaning", "planting", "teamwork", "basic gardening", "basic tech support", "patience", "communication", "physical work", "trash sorting", "team coordination", "reading aloud", "working with children", "storytelling");
                assertEquals(new HashSet<>(expected), new HashSet<>(postsFacade.getAllVolunteerPostsSkills()));
    }

    @Test
    void getAllPostsCities() {
        volunteeringFacade.addVolunteeringLocation(posterUsername, volunteeringId1, "loc1", new AddressTuple("city1", "street1", "num1"));
        volunteeringFacade.addVolunteeringLocation(posterUsername, volunteeringId1, "loc2", new AddressTuple("city2", "street2", "num2"));
        volunteeringFacade.addVolunteeringLocation(posterUsername, volunteeringId1, "loc3", new AddressTuple("city3", "street3", "num3"));
        volunteeringFacade.addVolunteeringLocation(posterUsername, volunteeringId2, "loc4", new AddressTuple("city4", "street4", "num4"));

        List<String> expectedVol1 = List.of("city1", "city2", "city3");
        List<String> resOnePost = postsFacade.getAllPostsCities();
        assertEquals(new HashSet<>(expectedVol1), new HashSet<>(resOnePost));

        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);

        List<String> expectedAll = List.of("city1", "city2", "city3", "city4");
        List<String> resAll = postsFacade.getAllPostsCities();
        assertEquals(new HashSet<>(expectedAll), new HashSet<>(resAll));
    }

    @Test
    void getAllPostsOrganizations() {
        int orgId2 = organizationsFacade.createOrganization("Org2", "Desc", "0541980766", "org@gmail.com", posterUsername);
        int volunteeringId3 = organizationsFacade.createVolunteering(orgId2, "Volunteering3", "This is a very cool volunteering.", posterUsername);

        int orgId3 = organizationsFacade.createOrganization("Org3", "Desc", "0541980766", "org@gmail.com", posterUsername);
        int volunteeringId4 = organizationsFacade.createVolunteering(orgId3, "Volunteering4", "This is a very cool volunteering.", posterUsername);

        List<String> expectedVol1 = List.of("Organization");
        List<String> resOnePost = postsFacade.getAllPostsOrganizations();
        assertEquals(new HashSet<>(expectedVol1), new HashSet<>(resOnePost));

        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId3);
        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId4);

        List<String> expectedAll = List.of("Organization", "Org2", "Org3");
        List<String> resAll = postsFacade.getAllPostsOrganizations();
        assertEquals(new HashSet<>(expectedAll), new HashSet<>(resAll));
    }

    @Test
    void getAllPostsVolunteerings() {
        int orgId2 = organizationsFacade.createOrganization("Org2", "Desc", "0541980766", "org@gmail.com", posterUsername);
        int volunteeringId3 = organizationsFacade.createVolunteering(orgId2, "Volunteering3", "This is a very cool volunteering.", posterUsername);

        int orgId3 = organizationsFacade.createOrganization("Org3", "Desc", "0541980766", "org@gmail.com", posterUsername);
        int volunteeringId4 = organizationsFacade.createVolunteering(orgId3, "Volunteering4", "This is a very cool volunteering.", posterUsername);

        List<String> expectedVol1 = List.of("Volunteering1");
        List<String> resOnePost = postsFacade.getAllPostsVolunteerings();
        assertEquals(new HashSet<>(expectedVol1), new HashSet<>(resOnePost));

        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId3);
        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId4);

        List<String> expectedAll = List.of("Volunteering1", "Volunteering3", "Volunteering4");
        List<String> resAll = postsFacade.getAllPostsVolunteerings();
        assertEquals(new HashSet<>(expectedAll), new HashSet<>(resAll));
    }

    @Test
    void getPostPastExperiences() {
        List<PastExperience> expectedBeforeLeaving = new ArrayList<>();
        List<PastExperience> resBeforeLeaving = postsFacade.getPostPastExperiences(volunteeringPostId);
        assertEquals(expectedBeforeLeaving, resBeforeLeaving);

        postsFacade.joinVolunteeringRequest(volunteeringPostId, nonPosterUsername, "I want to join.");
        volunteeringFacade.acceptUserJoinRequest(posterUsername, volunteeringId1, nonPosterUsername, 0);

        String experience = "It was very fun.";

        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        Date mockedDate = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());

        try (MockedConstruction<Date> mocked = mockConstruction(Date.class,
                (mock, context) -> {
                    when(mock.getTime()).thenReturn(mockedDate.getTime());
                })) {

            volunteeringFacade.finishVolunteering(nonPosterUsername, volunteeringId1, experience);

            List<PastExperience> expectedAfterLeaving = List.of(new PastExperience(nonPosterUsername, experience, mockedDate));
            List<PastExperience> resAfterLeaving = postsFacade.getPostPastExperiences(volunteeringPostId);
            assertEquals(expectedAfterLeaving, resAfterLeaving);
        }
    }

    @Test
    void givenExistingId_whenGetVolunteeringName_thenReturnName() {
        assertEquals("Volunteering1", postsFacade.getVolunteeringName(volunteeringId1));
        assertEquals("Volunteering2", postsFacade.getVolunteeringName(volunteeringId2));
    }

    @Test
    void givenNonExistingId_whenGetVolunteeringName_thenThrowException() {
        int id = Math.max(volunteeringId1, volunteeringId2) + 1;
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getVolunteeringName(id);
        });
        assertEquals("Volunteering with id " + id + " does not exist", exception.getMessage());
    }

    @Test
    void givenValidFields_whenCreateVolunteerPost_thenCreatePost() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("skills", List.of("skill1", "skill2"));
        map.put("categories", List.of("cat1", "cat2"));
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(map);

        when(ai.sendQuery(anyString()))
                .thenReturn("keyword1, keyword2, keyword3, keyword4")
                .thenReturn(jsonString);

        int numPostsBefore = postsFacade.getAllVolunteerPosts().size();

        postsFacade.createVolunteerPost(title, description, posterUsername);

        verify(spyKeywordExtractor).getVolunteerPostKeywords(eq(title), eq(description));
        verify(spySkillsAndCategoriesExtractor).getSkillsAndCategories(eq(title), eq(description), eq(null), eq(null));

        int numPostsAfter = postsFacade.getAllVolunteerPosts().size();
        assertEquals(1, numPostsAfter - numPostsBefore);
    }

    @Test
    void givenNonExistingUser_whenCreateVolunteerPost_thenThrowException() {
        int numPostsBefore = postsFacade.getAllVolunteerPosts().size();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.createVolunteerPost(title, description, newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        verify(spyKeywordExtractor, times(0)).getVolunteerPostKeywords(eq(title), eq(description));
        verify(spySkillsAndCategoriesExtractor, times(0)).getSkillsAndCategories(eq(title), eq(description), eq(null), eq(null));

        int numPostsAfter = postsFacade.getAllVolunteerPosts().size();
        assertEquals(0, numPostsAfter - numPostsBefore);
    }

    @Test
    void givenInvalidFields_whenCreateVolunteerPost_thenThrowException() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("skills", List.of("skill1", "skill2"));
        map.put("categories", List.of("cat1", "cat2"));
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(map);

        when(ai.sendQuery(anyString()))
                .thenReturn("keyword1, keyword2, keyword3, keyword4")
                .thenReturn(jsonString);

        int numPostsBefore = postsFacade.getAllVolunteerPosts().size();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.createVolunteerPost("", description, posterUsername);
        });
        assertEquals("Invalid post title: .\n", exception.getMessage());

        verify(spyKeywordExtractor, times(1)).getVolunteerPostKeywords(eq(""), eq(description));
        verify(spySkillsAndCategoriesExtractor, times(1)).getSkillsAndCategories(eq(""), eq(description), eq(null), eq(null));

        int numPostsAfter = postsFacade.getAllVolunteerPosts().size();
        assertEquals(0, numPostsAfter - numPostsBefore);
    }

    @Test
    void givenExistingPostByPoster_whenRemoveVolunteerPost_thenRemove() {
        assertDoesNotThrow(() -> postsFacade.getVolunteerPost(volunteerPostId, posterUsername));

        postsFacade.removeVolunteerPost(posterUsername, volunteerPostId);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getVolunteerPost(volunteerPostId, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId), exception.getMessage());

        verify(spyReportsFacade).removeVolunteerPostReports(volunteerPostId);

        String message = String.format("Your post \"%s\" was %s.", title, "removed");
        verifyMessage(message, List.of(posterUsername, otherManagerUsername));
    }

    @Test
    void givenNonExistingUser_whenRemoveVolunteerPost_thenThrowException() {
        assertDoesNotThrow(() -> postsFacade.getVolunteerPost(volunteerPostId, posterUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeVolunteerPost(newUser, volunteerPostId);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        assertDoesNotThrow(() -> postsFacade.getVolunteerPost(volunteerPostId, posterUsername));
        verifyNoInteractions(spyReportsFacade);
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingPost_whenRemoveVolunteerPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeVolunteerPost(posterUsername, volunteerPostId + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId + 1), exception.getMessage());
        verifyNoInteractions(spyReportsFacade);
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonPoster_whenRemoveVolunteerPost_thenThrowException() {
        assertDoesNotThrow(() -> postsFacade.getVolunteerPost(volunteerPostId, posterUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeVolunteerPost(otherManagerUsername, volunteerPostId);
        });
        assertEquals(PostErrors.makeUserIsNotAllowedToMakePostActionError(title, otherManagerUsername, "remove"), exception.getMessage());
        verifyNoInteractions(spyReportsFacade);
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        assertDoesNotThrow(() -> postsFacade.getVolunteerPost(volunteerPostId, posterUsername));
    }

    @ParameterizedTest
    @MethodSource("validVolunteerPostInputs")
    void givenExistingPostAndValidFieldsByPoster_whenEditVolunteerPost_thenEdit(String newTitle, String newDescription, String actor) {
        VolunteerPostDTO post = postsFacade.getVolunteerPost(volunteerPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));

        assertDoesNotThrow(() -> postsFacade.editVolunteerPost(volunteerPostId, newTitle, newDescription, actor));

        post = postsFacade.getVolunteerPost(volunteerPostId, posterUsername);
        assertTrue(verifyPostFields(post, newTitle, newDescription));

        verify(spyKeywordExtractor, times(1)).getVolunteerPostKeywords(eq(newTitle), eq(newDescription));

        String message = String.format("Your post \"%s\" was %s.", title, "edited");
        verifyMessage(message, List.of(posterUsername, otherManagerUsername));
    }

    @Test
    void givenNonExistingUser_whenEditVolunteerPost_thenThrowException() {
        VolunteerPostDTO post = postsFacade.getVolunteerPost(volunteerPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.editVolunteerPost(volunteerPostId, "title", "description", newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        post = postsFacade.getVolunteerPost(volunteerPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));

        verify(spyKeywordExtractor, times(0)).getVolunteerPostKeywords(Mockito.anyString(), Mockito.anyString());
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonValidFields_whenEditVolunteerPost_thenThrowException() {
        VolunteerPostDTO post = postsFacade.getVolunteerPost(volunteerPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));

        assertThrows(IllegalArgumentException.class, () -> postsFacade.editVolunteerPost(volunteerPostId, "", "", posterUsername));

        post = postsFacade.getVolunteerPost(volunteerPostId, posterUsername);
        assertTrue(verifyPostFields(post, title, description));

        verify(spyKeywordExtractor, times(1)).getVolunteerPostKeywords(Mockito.anyString(), Mockito.anyString());
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingPost_whenEditVolunteerPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.editVolunteerPost(volunteerPostId + 1, "title", "description", posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId + 1), exception.getMessage());

        verify(spyKeywordExtractor, times(0)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonPoster_whenEditVolunteerPost_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.editVolunteerPost(volunteerPostId, "title", "description", otherManagerUsername);
        });
        assertEquals(PostErrors.makeUserIsNotAllowedToMakePostActionError(title, otherManagerUsername, "edit"), exception.getMessage());

        verify(spyKeywordExtractor, times(0)).getVolunteeringPostKeywords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenPosterToExistingUserAndExistingPost_whenSendAddRelatedUserRequest_thenSend() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(nonPosterUsername, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", nonPosterUsername, "volunteer post", volunteerPostId), exception.getMessage());

        postsFacade.sendAddRelatedUserRequest(volunteerPostId, nonPosterUsername, posterUsername);

        assertDoesNotThrow(() -> postsFacade.getRequestRepository().getRequest(nonPosterUsername, volunteerPostId, RequestObject.VOLUNTEER_POST));

        String message = String.format("%s asked you to join the volunteer post \"%s\".", posterUsername, title);
        verifyMessage(message, List.of(nonPosterUsername));
    }

    @Test
    void givenNonExistingAssigner_whenSendAddRelatedUserRequest_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(nonPosterUsername, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", nonPosterUsername, "volunteer post", volunteerPostId), exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.sendAddRelatedUserRequest(volunteerPostId, nonPosterUsername, newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(nonPosterUsername, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", nonPosterUsername, "volunteer post", volunteerPostId), exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingAssignee_whenSendAddRelatedUserRequest_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(newUser, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", newUser, "volunteer post", volunteerPostId), exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.sendAddRelatedUserRequest(volunteerPostId, newUser, posterUsername);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(newUser, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", newUser, "volunteer post", volunteerPostId), exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingPost_whenSendAddRelatedUserRequest_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(nonPosterUsername, volunteerPostId + 1, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", nonPosterUsername, "volunteer post", volunteerPostId + 1), exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.sendAddRelatedUserRequest(volunteerPostId + 1, nonPosterUsername, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId + 1), exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(nonPosterUsername, volunteerPostId + 1, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", nonPosterUsername, "volunteer post", volunteerPostId + 1), exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenExistingRequest_whenSendAddRelatedUserRequest_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(nonPosterUsername, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", nonPosterUsername, "volunteer post", volunteerPostId), exception.getMessage());

        postsFacade.sendAddRelatedUserRequest(volunteerPostId, nonPosterUsername, posterUsername);

        String message = String.format("%s asked you to join the volunteer post \"%s\".", posterUsername, title);
        verifyMessage(message, List.of(nonPosterUsername));
        assertDoesNotThrow(() -> postsFacade.getRequestRepository().getRequest(nonPosterUsername, volunteerPostId, RequestObject.VOLUNTEER_POST));

        reset(sender);

        exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.sendAddRelatedUserRequest(volunteerPostId, nonPosterUsername, posterUsername);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d already exists.", nonPosterUsername, "volunteer post", volunteerPostId), exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenRelatedUser_whenSendAddRelatedUserRequest_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(otherManagerUsername, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", otherManagerUsername, "volunteer post", volunteerPostId), exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.sendAddRelatedUserRequest(volunteerPostId, otherManagerUsername, posterUsername);
        });
        assertEquals("User " + otherManagerUsername + " is already a related user of the post " + title + ".", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(otherManagerUsername, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", otherManagerUsername, "volunteer post", volunteerPostId), exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenAcceptValidRequest_whenHandleAddRelatedUserRequest_thenAddUser() {
        assertFalse(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(nonPosterUsername));

        postsFacade.sendAddRelatedUserRequest(volunteerPostId, nonPosterUsername, posterUsername);

        reset(sender);

        postsFacade.handleAddRelatedUserRequest(volunteerPostId, nonPosterUsername, true);

        assertTrue(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(nonPosterUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(nonPosterUsername, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", nonPosterUsername, "volunteer post", volunteerPostId), exception.getMessage());

        String message = String.format("%s has %s your request to join the post \"%s\".", nonPosterUsername, "approved", title);
        verifyMessage(message, List.of(posterUsername));
    }

    @Test
    void givenDenyValidRequest_whenHandleAddRelatedUserRequest_thenNoAddedUser() {
        assertFalse(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(nonPosterUsername));

        postsFacade.sendAddRelatedUserRequest(volunteerPostId, nonPosterUsername, posterUsername);

        reset(sender);
        postsFacade.handleAddRelatedUserRequest(volunteerPostId, nonPosterUsername, false);

        assertFalse(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(nonPosterUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRequestRepository().getRequest(nonPosterUsername, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", nonPosterUsername, "volunteer post", volunteerPostId), exception.getMessage());

        String message = String.format("%s has %s your request to join the post \"%s\".", nonPosterUsername, "denied", title);
        verifyMessage(message, List.of(posterUsername));
    }

    @Test
    void givenNonExistingUser_whenHandleAddRelatedUserRequest_thenThrowException() {
        assertFalse(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(newUser));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.handleAddRelatedUserRequest(volunteerPostId, newUser, true);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        assertFalse(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(newUser));

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingRequest_whenHandleAddRelatedUserRequest_thenThrowException() {
        assertFalse(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(nonPosterUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.handleAddRelatedUserRequest(volunteerPostId, nonPosterUsername, true);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", nonPosterUsername, "volunteer post", volunteerPostId), exception.getMessage());

        assertFalse(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(nonPosterUsername));

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenRelatedUser_whenRemoveRelatedUser_thenRemove() {
        postsFacade.sendAddRelatedUserRequest(volunteerPostId, nonPosterUsername, posterUsername);
        postsFacade.handleAddRelatedUserRequest(volunteerPostId, nonPosterUsername, true);
        reset(sender);
        assertTrue(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(nonPosterUsername));

        postsFacade.removeRelatedUser(volunteerPostId, nonPosterUsername, posterUsername);

        assertFalse(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(nonPosterUsername));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(sender).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        verify(sender, times(3)).sendNotification(recipientCaptor.capture(), captor.capture());

        List<String> recipients = recipientCaptor.getAllValues();
        List<Notification> notifications = captor.getAllValues();

        assertTrue(recipients.subList(0, 2).containsAll(List.of(posterUsername, otherManagerUsername)));
        for (Notification notification : notifications.subList(0, 2)) {
            assertEquals(String.format("%s was removed from the post \"%s\".", nonPosterUsername, title), notification.getMessage());
        }

        assertTrue(recipients.subList(2, 3).containsAll(List.of(nonPosterUsername)));
        for (Notification notification : notifications.subList(2, 3)) {
            assertEquals(String.format("You were removed from the post \"%s\".", title), notification.getMessage());
        }
    }

    @Test
    void givenNoExistingActor_whenRemoveRelatedUser_thenThrowException() {
        assertTrue(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(otherManagerUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeRelatedUser(volunteerPostId, otherManagerUsername, newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        assertTrue(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(otherManagerUsername));

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNoExistingPost_whenRemoveRelatedUser_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeRelatedUser(volunteerPostId + 1, otherManagerUsername, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId + 1), exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonPosterActor_whenRemoveRelatedUser_thenThrowException() {
        postsFacade.sendAddRelatedUserRequest(volunteerPostId, nonPosterUsername, posterUsername);
        postsFacade.handleAddRelatedUserRequest(volunteerPostId, nonPosterUsername, true);
        reset(sender);

        assertTrue(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(nonPosterUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeRelatedUser(volunteerPostId, nonPosterUsername, otherManagerUsername);
        });
        assertEquals(PostErrors.makeUserIsNotAllowedToMakePostActionError(title, otherManagerUsername, "remove user from"), exception.getMessage());

        assertTrue(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(nonPosterUsername));

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenPosterRemoved_whenRemoveRelatedUser_thenThrowException() {
        assertTrue(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(posterUsername));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeRelatedUser(volunteerPostId, posterUsername, posterUsername);
        });
        assertEquals(PostErrors.makePosterCanNotBeRemovedFromPost(posterUsername, title), exception.getMessage());

        assertTrue(postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getRelatedUsers().contains(posterUsername));

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    void givenNonRelatedUserRemoved_whenRemoveRelatedUser_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeRelatedUser(volunteerPostId, newUser, posterUsername);
        });
        assertEquals(PostErrors.makeUserIsRelatedToPost(newUser, title, false), exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenRelatedUser_whenHasRelatedUser_thenReturnTrue() {
        assertTrue(postsFacade.hasRelatedUser(volunteerPostId, posterUsername));
        assertTrue(postsFacade.hasRelatedUser(volunteerPostId, otherManagerUsername));
    }

    @Test
    void givenNotRelatedUser_whenHasRelatedUser_thenReturnFalse() {
        assertFalse(postsFacade.hasRelatedUser(volunteerPostId, admin));
        assertFalse(postsFacade.hasRelatedUser(volunteerPostId, nonPosterUsername));
    }

    @Test
    void givenNoExistingUser_whenHasRelatedUser_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.hasRelatedUser(volunteerPostId, newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());
    }

    @Test
    void givenNoExistingPost_whenHasRelatedUser_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.hasRelatedUser(volunteerPostId + 1, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId + 1), exception.getMessage());
    }

    @Test
    void givenExistingPost_whenGetRelatedUsers_thenReturnUsers() {
        List<String> expected1 = List.of(posterUsername, otherManagerUsername);
        List<String> expected2 = List.of(posterUsername, otherManagerUsername, nonPosterUsername, admin);

        List<String> res1 = postsFacade.getRelatedUsers(volunteerPostId);

        postsFacade.sendAddRelatedUserRequest(volunteerPostId, nonPosterUsername, posterUsername);
        postsFacade.sendAddRelatedUserRequest(volunteerPostId, admin, posterUsername);
        postsFacade.handleAddRelatedUserRequest(volunteerPostId, nonPosterUsername, true);
        postsFacade.handleAddRelatedUserRequest(volunteerPostId, admin, true);

        List<String> res2 = postsFacade.getRelatedUsers(volunteerPostId);

        assertEquals(new HashSet<>(expected1), new HashSet<>(res1));
        assertEquals(new HashSet<>(expected2), new HashSet<>(res2));
    }

    @Test
    void givenNonExistingPost_whenGetRelatedUsers_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getRelatedUsers(volunteerPostId + 1);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId + 1), exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Poster", "Manager"})
    public void givenNewImageByPosterOrRelatedUser_whenAddImage_thenAdd(String actor) {
        String newPath = "dummyPath";
        List<String> expected = List.of(newPath);

        assertEquals(new ArrayList<String>(), postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
        postsFacade.addImage(volunteerPostId, newPath, actor);
        assertEquals(expected, postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
    }

    @Test
    public void givenNoExistingUser_whenAddImage_thenThrowException() {
        String newPath = "dummyPath";

        assertEquals(new ArrayList<String>(), postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.addImage(volunteerPostId, newPath, newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        assertEquals(new ArrayList<String>(), postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
    }

    @Test
    public void givenNonExistingPost_whenAddImage_thenThrowException() {
        String newPath = "dummyPath";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.addImage(volunteerPostId + 1, newPath, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId + 1), exception.getMessage());
    }

    @Test
    public void givenNonRelatedUser_whenAddImage_thenThrowException() {
        String newPath = "dummyPath";

        assertEquals(new ArrayList<String>(), postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.addImage(volunteerPostId, newPath, nonPosterUsername);
        });
        String expectedErr = PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, nonPosterUsername, "add image to");
        assertEquals(expectedErr, exception.getMessage());

        assertEquals(new ArrayList<String>(), postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
    }

    @ParameterizedTest
    @ValueSource(strings = {"dummyPath", "\"dummyPath\""})
    public void givenExistingImage_whenAddImage_thenThrowException(String existingPath) {
        String newPath = "dummyPath";
        postsFacade.addImage(volunteerPostId, newPath, posterUsername);

        List<String> expected = List.of(newPath);
        assertEquals(expected, postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.addImage(volunteerPostId, newPath, posterUsername);
        });
        String expectedErr = PostErrors.makeImagePathExists(newPath, posterUsername, true);
        assertEquals(expectedErr, exception.getMessage());

        assertEquals(expected, postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    public void givenInvalidImage_whenAddImage_thenThrowException(String invalidPath) {
        assertEquals(new ArrayList<String>(), postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.addImage(volunteerPostId, invalidPath, posterUsername);
        });
        String expectedErr = PostErrors.makeImagePathIsNotValid(invalidPath);
        assertEquals(expectedErr, exception.getMessage());

        assertEquals(new ArrayList<String>(), postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Poster", "Manager"})
    public void givenExistingImageByPosterOrManager_whenRemoveImage_thenRemove(String actor) {
        String newPath = "dummyPath";
        postsFacade.addImage(volunteerPostId, newPath, posterUsername);

        List<String> expected = List.of(newPath);
        assertEquals(expected, postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());

        postsFacade.removeImage(volunteerPostId, newPath, actor);

        assertEquals(new ArrayList<>(), postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
    }

    @Test
    public void givenNoExistingUser_whenRemoveImage_thenThrowException() {
        String newPath = "dummyPath";
        postsFacade.addImage(volunteerPostId, newPath, posterUsername);

        List<String> expected = List.of(newPath);
        assertEquals(expected, postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.addImage(volunteerPostId, newPath, newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());

        assertEquals(expected, postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
    }

    @Test
    public void givenNonExistingPost_whenRemoveImage_thenThrowException() {
        String newPath = "dummyPath";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeImage(volunteerPostId + 1, newPath, posterUsername);
        });
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId + 1), exception.getMessage());
    }

    @Test
    public void givenNotRelatedUser_whenRemoveImage_thenThrowException() {
        String newPath = "dummyPath";
        postsFacade.addImage(volunteerPostId, newPath, posterUsername);

        List<String> expected = List.of(newPath);
        assertEquals(expected, postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeImage(volunteerPostId, newPath, nonPosterUsername);
        });
        String expectedErr = PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, nonPosterUsername, "remove image from");
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(expected, postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
    }

    @Test
    public void givenNonExistingImage_whenRemoveImage_thenThrowException() {
        String newPath = "dummyPath";

        assertEquals(new ArrayList<>(), postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.removeImage(volunteerPostId, newPath, posterUsername);
        });
        String expectedErr = PostErrors.makeImagePathExists(newPath, posterUsername, false);
        assertEquals(expectedErr, exception.getMessage());
        assertEquals(new ArrayList<>(), postsFacade.getVolunteerPost(volunteerPostId, posterUsername).getImages());
    }

    @Test
    public void givenVolunteeringPostsAndSearchString_whenSearchByKeywords_thenReturnPosts() {
        when(ai.sendQuery(anyString())).thenAnswer(invocation -> {
            String prompt = invocation.getArgument(0);
            if (prompt.contains("Animal Shelter Help")) {
                return "animals, care, shelter, compassion";
            } else if (prompt.contains("Community Garden Project")) {
                return "plants, gardening, community, nature";
            } else if (prompt.contains("Elderly Tech Support")) {
                return "technology, elderly, support, community";
            } else if (prompt.contains("Beach Cleanup")) {
                return "environment, cleanup, nature, teamwork";
            } else if (prompt.contains("Library Reading Buddy")) {
                return "reading, kids, books, community";
            } else {
                return "volunteer, local, initiative, group";
            }
        });

        volunteeringFacade.updateVolunteeringSkills(posterUsername, volunteeringId1, List.of("reading"));
        volunteeringFacade.updateVolunteeringSkills(posterUsername, volunteeringId2, List.of("animals"));

        int postId1 = postsFacade.createVolunteeringPost("Animal Shelter Help", "Looking for volunteers to help feed and care for dogs and cats.", posterUsername, volunteeringId1);
        int postId2 = postsFacade.createVolunteeringPost("Community Garden Project", "Join us in planting and maintaining a local community garden.", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Elderly Tech Support", "Assist elderly community members with basic smartphone and computer skills.", posterUsername, volunteeringId1);
        int postId4 = postsFacade.createVolunteeringPost("Beach Cleanup", "Help clean up our local beach and protect marine life.", posterUsername, volunteeringId2);
        int postId5 = postsFacade.createVolunteeringPost("Library Reading Buddy", "Read books to kids at the local library every weekend.", posterUsername, volunteeringId2);
        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(postId1, posterUsername);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);
        VolunteeringPostDTO post4 = postsFacade.getVolunteeringPost(postId4, posterUsername);
        VolunteeringPostDTO post5 = postsFacade.getVolunteeringPost(postId5, posterUsername);
        List<PostDTO> allPosts = List.of(post1, post2, post3, post4, post5);

        List<PostDTO> expectedCommunity = List.of(post2, post3, post5);
        List<PostDTO> expectedNature = List.of(post2, post4);
        List<PostDTO> expectedCommunityAndNature = List.of(post2, post3, post4, post5);
        List<PostDTO> expectedReading = List.of(post1, post2, post3, post5);
        List<PostDTO> expectedAnimals = List.of(post1, post4, post5);

        List<VolunteeringPostDTO> resCommunity = (List<VolunteeringPostDTO>) postsFacade.searchByKeywords(" commUnity", posterUsername, allPosts, true);
        List<VolunteeringPostDTO> resNature = (List<VolunteeringPostDTO>) postsFacade.searchByKeywords("nAtuRe   ", posterUsername, allPosts, true);
        List<VolunteeringPostDTO> resCommunityAndNature = (List<VolunteeringPostDTO>) postsFacade.searchByKeywords(" commUnity and nature", posterUsername, allPosts, true);
        List<VolunteeringPostDTO> resReading = (List<VolunteeringPostDTO>) postsFacade.searchByKeywords("reaD   ", posterUsername, allPosts, true);
        List<VolunteeringPostDTO> resAnimals = (List<VolunteeringPostDTO>) postsFacade.searchByKeywords(" animal", posterUsername, allPosts, true);

        assertEquals(new HashSet<>(expectedCommunity), new HashSet<>(resCommunity));
        assertEquals(new HashSet<>(expectedNature), new HashSet<>(resNature));
        assertEquals(new HashSet<>(expectedCommunityAndNature), new HashSet<>(resCommunityAndNature));
        assertEquals(new HashSet<>(expectedReading), new HashSet<>(resReading));
        assertEquals(new HashSet<>(expectedAnimals), new HashSet<>(resAnimals));
    }

    @Test
    public void givenEmptySearchStringInVolunteeringPosts_whenSearchByKeywords_thenReturnPosts() {
        volunteeringFacade.updateVolunteeringSkills(posterUsername, volunteeringId1, List.of("reading"));
        volunteeringFacade.updateVolunteeringSkills(posterUsername, volunteeringId2, List.of("animals"));

        int postId1 = postsFacade.createVolunteeringPost("Animal Shelter Help", "Looking for volunteers to help feed and care for dogs and cats.", posterUsername, volunteeringId1);
        int postId2 = postsFacade.createVolunteeringPost("Community Garden Project", "Join us in planting and maintaining a local community garden.", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Elderly Tech Support", "Assist elderly community members with basic smartphone and computer skills.", posterUsername, volunteeringId1);
        int postId4 = postsFacade.createVolunteeringPost("Beach Cleanup", "Help clean up our local beach and protect marine life.", posterUsername, volunteeringId2);
        int postId5 = postsFacade.createVolunteeringPost("Library Reading Buddy", "Read books to kids at the local library every weekend.", posterUsername, volunteeringId2);
        VolunteeringPostDTO post0 = postsFacade.getVolunteeringPost(volunteeringPostId, posterUsername);
        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(postId1, posterUsername);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);
        VolunteeringPostDTO post4 = postsFacade.getVolunteeringPost(postId4, posterUsername);
        VolunteeringPostDTO post5 = postsFacade.getVolunteeringPost(postId5, posterUsername);
        List<PostDTO> allPosts = List.of(post0, post1, post2, post3, post4, post5);

        List<VolunteeringPostDTO> res = (List<VolunteeringPostDTO>) postsFacade.searchByKeywords("        ", posterUsername, allPosts, true);

        assertEquals(new HashSet<>(allPosts), new HashSet<>(res));
    }

    private void setupVolunteerPostMock() {
        Map<String, Object> map = new HashMap<>();

        AtomicInteger counter = new AtomicInteger(0);
        when(ai.sendQuery(anyString())).thenAnswer(invocation -> {
            String prompt = invocation.getArgument(0);
            if (counter.getAndIncrement() % 2 == 0) {
                if (prompt.contains("Animal Shelter Help")) {
                    return "animals, care, shelter, compassion";
                } else if (prompt.contains("Community Garden Project")) {
                    return "plants, gardening, community, nature";
                } else if (prompt.contains("Elderly Tech Support")) {
                    return "technology, elderly, support, community";
                } else if (prompt.contains("Beach Cleanup")) {
                    return "environment, cleanup, nature, teamwork";
                } else if (prompt.contains("Library Reading Buddy")) {
                    return "reading, kids, books, community";
                } else {
                    return "volunteer, local, initiative, group";
                }
            }
            else {
                if (prompt.contains("Animal Shelter Help")) {
                    map.put("skills", List.of("animal care", "empathy", "cleaning"));
                    map.put("categories", List.of("Animals", "Welfare"));
                    return (new ObjectMapper()).writeValueAsString(map);
                } else if (prompt.contains("Community Garden Project")) {
                    map.put("skills", List.of("planting", "teamwork", "basic gardening"));
                    map.put("categories", List.of("Environment", "Community"));
                    return (new ObjectMapper()).writeValueAsString(map);
                } else if (prompt.contains("Elderly Tech Support")) {
                    map.put("skills", List.of("basic tech support", "patience", "communication"));
                    map.put("categories", List.of("Education", "Community"));
                    return (new ObjectMapper()).writeValueAsString(map);
                } else if (prompt.contains("Beach Cleanup")) {
                    map.put("skills", List.of("physical work", "trash sorting", "team coordination"));
                    map.put("categories", List.of("Environment", "Public Health"));
                    return (new ObjectMapper()).writeValueAsString(map);
                } else if (prompt.contains("Library Reading Buddy")) {
                    map.put("skills", List.of("reading aloud", "working with children", "storytelling"));
                    map.put("categories", List.of("Education", "Youth"));
                    return (new ObjectMapper()).writeValueAsString(map);
                } else {
                    map.put("skills", List.of("planting", "teamwork", "basic gardening"));
                    map.put("categories", List.of("Environment", "Community"));
                    return (new ObjectMapper()).writeValueAsString(map);
                }
            }
        });

    }

    @Test
    public void givenVolunteerPosts_whenSearchByKeywords_thenReturnPosts() {
        setupVolunteerPostMock();
        int postId1 = postsFacade.createVolunteerPost("Animal Shelter Help", "Looking for volunteers to help feed and care for dogs and cats.", posterUsername);
        int postId2 = postsFacade.createVolunteerPost("Community Garden Project", "Join us in planting and maintaining a local community garden.", posterUsername);
        int postId3 = postsFacade.createVolunteerPost("Elderly Tech Support", "Assist elderly community members with basic smartphone and computer skills.", posterUsername);
        int postId4 = postsFacade.createVolunteerPost("Beach Cleanup", "Help clean up our local beach and protect marine life.", posterUsername);
        int postId5 = postsFacade.createVolunteerPost("Library Reading Buddy", "Read books to kids at the local library every weekend.", posterUsername);
        VolunteerPostDTO post1 = postsFacade.getVolunteerPost(postId1, posterUsername);
        VolunteerPostDTO post2 = postsFacade.getVolunteerPost(postId2, posterUsername);
        VolunteerPostDTO post3 = postsFacade.getVolunteerPost(postId3, posterUsername);
        VolunteerPostDTO post4 = postsFacade.getVolunteerPost(postId4, posterUsername);
        VolunteerPostDTO post5 = postsFacade.getVolunteerPost(postId5, posterUsername);
        List<PostDTO> allPosts = List.of(post1, post2, post3, post4, post5);

        List<PostDTO> expectedCommunity = List.of(post2, post3, post5);
        List<PostDTO> expectedNature = List.of(post2, post4);
        List<PostDTO> expectedTechnology = List.of(post3);
        List<PostDTO> expectedReading = List.of(post5);
        List<PostDTO> expectedTeamwork = List.of(post2, post4);
        List<PostDTO> expectedAnimals = List.of(post1);
        List<PostDTO> expectedEnvironment = List.of(post2, post4);
        List<PostDTO> expectedEducation = List.of(post3, post5);

        List<VolunteerPostDTO> resCommunity = (List<VolunteerPostDTO>) postsFacade.searchByKeywords("community", posterUsername, allPosts, false);
        List<VolunteerPostDTO> resNature = (List<VolunteerPostDTO>) postsFacade.searchByKeywords("nature", posterUsername, allPosts, false);
        List<VolunteerPostDTO> resTechnology = (List<VolunteerPostDTO>) postsFacade.searchByKeywords("Technology", posterUsername, allPosts, false);
        List<VolunteerPostDTO> resReading = (List<VolunteerPostDTO>) postsFacade.searchByKeywords("read", posterUsername, allPosts, false);
        List<VolunteerPostDTO> resTeamwork = (List<VolunteerPostDTO>) postsFacade.searchByKeywords("team", posterUsername, allPosts, false);
        List<VolunteerPostDTO> resAnimals = (List<VolunteerPostDTO>) postsFacade.searchByKeywords("animal", posterUsername, allPosts, false);
        List<VolunteerPostDTO> resEnvironment = (List<VolunteerPostDTO>) postsFacade.searchByKeywords("env", posterUsername, allPosts, false);
        List<VolunteerPostDTO> resEducation = (List<VolunteerPostDTO>) postsFacade.searchByKeywords("Education", posterUsername, allPosts, false);

        assertEquals(new HashSet<>(expectedCommunity), new HashSet<>(resCommunity));
        assertEquals(new HashSet<>(expectedNature), new HashSet<>(resNature));
        assertEquals(new HashSet<>(expectedTechnology), new HashSet<>(resTechnology));
        assertEquals(new HashSet<>(expectedReading), new HashSet<>(resReading));
        assertEquals(new HashSet<>(expectedTeamwork), new HashSet<>(resTeamwork));
        assertEquals(new HashSet<>(expectedAnimals), new HashSet<>(resAnimals));
        assertEquals(new HashSet<>(expectedEnvironment), new HashSet<>(resEnvironment));
        assertEquals(new HashSet<>(expectedEducation), new HashSet<>(resEducation));
    }

    @Test
    public void givenEmptySearchStringInVolunteerPosts_whenSearchByKeywords_thenReturnPosts() throws JsonProcessingException {
        int postId1 = postsFacade.createVolunteerPost("Animal Shelter Help", "Looking for volunteers to help feed and care for dogs and cats.", posterUsername);
        int postId2 = postsFacade.createVolunteerPost("Community Garden Project", "Join us in planting and maintaining a local community garden.", posterUsername);
        int postId3 = postsFacade.createVolunteerPost("Elderly Tech Support", "Assist elderly community members with basic smartphone and computer skills.", posterUsername);
        int postId4 = postsFacade.createVolunteerPost("Beach Cleanup", "Help clean up our local beach and protect marine life.", posterUsername);
        int postId5 = postsFacade.createVolunteerPost("Library Reading Buddy", "Read books to kids at the local library every weekend.", posterUsername);
        VolunteerPostDTO post0 = postsFacade.getVolunteerPost(volunteerPostId, posterUsername);
        VolunteerPostDTO post1 = postsFacade.getVolunteerPost(postId1, posterUsername);
        VolunteerPostDTO post2 = postsFacade.getVolunteerPost(postId2, posterUsername);
        VolunteerPostDTO post3 = postsFacade.getVolunteerPost(postId3, posterUsername);
        VolunteerPostDTO post4 = postsFacade.getVolunteerPost(postId4, posterUsername);
        VolunteerPostDTO post5 = postsFacade.getVolunteerPost(postId5, posterUsername);
        List<PostDTO> allPosts = List.of(post0, post1, post2, post3, post4, post5);

        List<VolunteerPostDTO> res = (List<VolunteerPostDTO>) postsFacade.searchByKeywords("  ", posterUsername, allPosts, false);
        res.sort(Comparator.comparing(VolunteerPostDTO::getId));

        assertEquals(6, res.size());
        assertEquals(post0, res.get(0));
        assertEquals(post1, res.get(1));
        assertEquals(post2, res.get(2));
        assertEquals(post3, res.get(3));
        assertEquals(post4, res.get(4));
        assertEquals(post5, res.get(5));
    }

    @ParameterizedTest
    @MethodSource("numJoiners")
    void givenPosts_whenSortByPostingTime_thenSort(int post1Day, int post2Day, int post3Day) {
        LocalDateTime mockedDate1 = LocalDateTime.of(2025, 1, post1Day, 10, 0);
        LocalDateTime mockedDate2 = LocalDateTime.of(2025, 1, post2Day, 10, 0);
        LocalDateTime mockedDate3 = LocalDateTime.of(2025, 1, post3Day, 10, 0);

        int postId1 = postsFacade.createVolunteeringPost("Title1", "Description1", posterUsername, volunteeringId1);
        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);

        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(postId1, posterUsername);
        post1.setPostedTime(mockedDate1);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);
        post2.setPostedTime(mockedDate2);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);
        post3.setPostedTime(mockedDate3);
        List<PostDTO> allPosts = List.of(post1, post2, post3);

        PostDTO[] expected = new PostDTO[3];
        expected[3 - post1Day] = post1;
        expected[3 - post2Day] = post2;
        expected[3 - post3Day] = post3;

        List<PostDTO> res = postsFacade.sortByPostingTime(posterUsername, allPosts);

        assertEquals(Arrays.asList(expected), res);
    }

    @Test
    void givenNonExistingUser_whenSortByPostingTime_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.sortByPostingTime(newUser, new ArrayList<>());
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("numJoiners")
    void givenPosts_whenSortByLastEditTime_thenSort(int post1Day, int post2Day, int post3Day) {
        LocalDateTime mockedDate1 = LocalDateTime.of(2025, 1, post1Day, 10, 0);
        LocalDateTime mockedDate2 = LocalDateTime.of(2025, 1, post2Day, 10, 0);
        LocalDateTime mockedDate3 = LocalDateTime.of(2025, 1, post3Day, 10, 0);

        int postId1 = postsFacade.createVolunteeringPost("Title1", "Description1", posterUsername, volunteeringId1);
        int postId2 = postsFacade.createVolunteeringPost("Title2", "Description2", posterUsername, volunteeringId1);
        int postId3 = postsFacade.createVolunteeringPost("Title3", "Description3", posterUsername, volunteeringId2);

        VolunteeringPostDTO post1 = postsFacade.getVolunteeringPost(postId1, posterUsername);
        post1.setLastEditedTime(mockedDate1);
        VolunteeringPostDTO post2 = postsFacade.getVolunteeringPost(postId2, posterUsername);
        post2.setLastEditedTime(mockedDate2);
        VolunteeringPostDTO post3 = postsFacade.getVolunteeringPost(postId3, posterUsername);
        post3.setLastEditedTime(mockedDate3);
        List<PostDTO> allPosts = List.of(post1, post2, post3);

        PostDTO[] expected = new PostDTO[3];
        expected[3 - post1Day] = post1;
        expected[3 - post2Day] = post2;
        expected[3 - post3Day] = post3;

        List<PostDTO> res = postsFacade.sortByLastEditTime(posterUsername, allPosts);

        assertEquals(Arrays.asList(expected), res);
    }

    @Test
    void givenNonExistingUser_whenSortByLastEditTime_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.sortByLastEditTime(newUser, new ArrayList<>());
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());
    }

    @Test
    void givenHasPosts_whenHasPosts_thenReturnTrue() {
        assertTrue(postsFacade.hasPosts(volunteeringId1));
    }

    @Test
    void givenDoesNotHavePosts_whenHasPosts_thenReturnFalse() {
        assertFalse(postsFacade.hasPosts(volunteeringId2));
    }

    @Test
    void givenExistingUser_whenGetUserRequests_thenReturnRequests() {
        List<Request> resBefore = postsFacade.getUserRequests(nonPosterUsername);
        assertEquals(new ArrayList<>(), resBefore);

        postsFacade.sendAddRelatedUserRequest(volunteerPostId, nonPosterUsername, posterUsername);
        List<Request> resAfter = postsFacade.getUserRequests(nonPosterUsername);

        assertEquals(1, resAfter.size());
        Request request = resAfter.get(0);
        Request expected = new Request(nonPosterUsername, posterUsername, volunteerPostId, RequestObject.VOLUNTEER_POST);
        expected.setDate(request.getDate());

        assertEquals(expected, request);
    }

    @Test
    void givenNonExistingUser_whenGetUserRequests_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            postsFacade.getUserRequests(newUser);
        });
        assertEquals("User NewUser doesn't exist", exception.getMessage());
    }
}
