package com.dogood.dogoodbackend.acceptance;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest;
import com.dogood.dogoodbackend.api.userrequests.VerifyEmailRequest;
import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.posts.PostDTO;
import com.dogood.dogoodbackend.domain.posts.VolunteerPostDTO;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
import com.dogood.dogoodbackend.domain.reports.Report;
import com.dogood.dogoodbackend.domain.reports.ReportDTO;
import com.dogood.dogoodbackend.domain.requests.RequestObject;
import com.dogood.dogoodbackend.domain.users.notificiations.Notification;
import com.dogood.dogoodbackend.domain.volunteerings.AddressTuple;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.emailverification.VerificationData;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.*;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import com.dogood.dogoodbackend.utils.PostErrors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class PostAcceptanceTests {
    @MockitoBean
    NotificationSocketSender notificationSocketSender;

    @MockitoBean
    FirebaseMessaging firebaseMessaging;

    @MockitoBean
    Gemini gemini;

    @Autowired
    PostService postService;

    @MockitoSpyBean
    ReportService reportService;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    UserService userService;

    @Autowired
    VolunteeringService volunteeringService;

    @Autowired
    FacadeManager facadeManager;

    @MockitoBean
    EmailSender emailSender;

    @MockitoBean
    VerificationCacheService verificationCacheService;

    private final String aliceId = "Alice";
    private final String bobId = "BobTheBuilder";
    private final String adminId = "Admin";

    private String aliceToken;
    private String bobToken;
    private String adminToken;

    private final String postTitle = "Post";
    private final String postDescription = "Description";

    private int organizationId;
    private int volunteeringId;
    private int volunteeringPostId;
    private int volunteerPostId;

    @Autowired
    MessageJPA messageJPA;
    @Autowired
    VolunteeringJPA volunteeringJPA;
    @Autowired
    OrganizationJPA organizationJPA;
    @Autowired
    NotificationJPA notificationJPA;
    @Autowired
    UserJPA userJPA;
    @Autowired
    VolunteerPostJPA volunteerPostJPA;
    @Autowired
    VolunteeringPostJPA volunteeringPostJPA;
    @Autowired
    ReportJPA reportJPA;

    @BeforeEach
    void setUp() {
        messageJPA.deleteAll();
        volunteeringJPA.deleteAll();
        organizationJPA.deleteAll();
        notificationJPA.deleteAll();
        userJPA.deleteAll();
        volunteerPostJPA.deleteAll();
        reportJPA.deleteAll();
        volunteeringPostJPA.deleteAll();

        userService.register(aliceId, "123456", "Alice Alice", "alice@dogood.com", "052-0520520", new Date(), null);
        userService.register(bobId, "123456", "Bob Bob", "bob@dogood.com", "052-0520520", new Date(), null);
        facadeManager.getUsersFacade().registerAdmin(adminId, "123456", "Admin Admin", "admin@dogood.com", "052-0520520", new Date());

        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(adminId),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail("admin@dogood.com");
                    return Optional.of(new VerificationData("", Instant.MAX,request));
                });
        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(aliceId),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail("alice@dogood.com");
                    return Optional.of(new VerificationData("", Instant.MAX,request));
                });
        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(bobId),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail("bob@dogood.com");
                    return Optional.of(new VerificationData("", Instant.MAX,request));
                });

        VerifyEmailRequest orgEmailRequest = new VerifyEmailRequest(adminId,"");
        VerifyEmailRequest alEmailRequest = new VerifyEmailRequest(aliceId,"");
        VerifyEmailRequest bobEmailRequest = new VerifyEmailRequest(bobId,"");
        userService.verifyEmail(orgEmailRequest);
        userService.verifyEmail(alEmailRequest);
        userService.verifyEmail(bobEmailRequest);

        Response<String> login1 = userService.login(aliceId, "123456");
        Response<String> login2 = userService.login(bobId, "123456");
        Response<String> login3 = userService.login(adminId, "123456");

        aliceToken = login1.getData();
        bobToken = login2.getData();
        adminToken = login3.getData();

        this.organizationId = -1;
        this.volunteeringId = -1;
        this.volunteeringPostId = -1;
        this.volunteerPostId = -1;
    }

    private void createVolunteeringPostByAlice() {
        Response<Integer> createOrganization = organizationService.createOrganization(aliceToken, "Organization", "Description", "052-0520520", "organization@manager.com", aliceId);
        this.organizationId = createOrganization.getData();
        Response<Integer> createVolunteering = organizationService.createVolunteering(aliceToken, organizationId, "Volunteering", "Description", aliceId);
        this.volunteeringId = createVolunteering.getData();
        Response<Integer> createVolunteeringPost = postService.createVolunteeringPost(aliceToken, postTitle, postDescription, aliceId, volunteeringId);
        this.volunteeringPostId = createVolunteeringPost.getData();
    }

    private void createVolunteerPostByAliceAndAddBob() {
        createVolunteerPostByAlice();

        postService.sendAddRelatedUserRequest(aliceToken, aliceId, volunteerPostId, bobId);
        postService.handleAddRelatedUserRequest(bobToken, bobId, volunteerPostId, true);
    }

    private void createVolunteerPostByAlice() {
        Response<Integer> createVolunteerPost = postService.createVolunteerPost(aliceToken, aliceId, postTitle, postDescription);
        this.volunteerPostId = createVolunteerPost.getData();
    }

    private void makeBobManager() {
        organizationService.sendAssignManagerRequest(aliceToken, bobId, aliceId, organizationId);
        organizationService.handleAssignManagerRequest(bobToken, bobId, organizationId, true);
    }

    private void assertMessages(String username, String token, List<String> messages, int numMessages) {
        List<Notification> userNotifications = userService.getUserNotifications(token, username).getData();
        List<String> userNotificationMessages = userNotifications.stream().map(notification -> notification.getMessage()).collect(Collectors.toList());

        for(int i = 0; i < numMessages; i++) {
            assertEquals(messages.get(i), userNotificationMessages.get(i));
        }
    }

    private boolean isVolunteer(String username, String token, int volunteeringId) {
        Response<VolunteeringDTO> getVolunteeringResult = volunteeringService.getVolunteeringDTO(token, username, volunteeringId);
        boolean volunteerOrManager = !getVolunteeringResult.getError();
        if(!volunteerOrManager)
            return false;

        int organizationId = getVolunteeringResult.getData().getOrgId();
        boolean manager = organizationService.isManager(token, username, organizationId).getData();
        return !manager;
    }

    // 2.4, 2.5, 4.7
    @Test
    void givenPostAndAcceptRequest_whenGetVolunteeringPostAndJoinVolunteering_thenAddVolunteer() {
        when(gemini.sendQuery(anyString())).thenReturn("");

        createVolunteeringPostByAlice();

        Response<VolunteeringPostDTO> getVolunteeringPost = postService.getVolunteeringPost(bobToken, volunteeringPostId, bobId);
        VolunteeringPostDTO volunteeringPostDTO = getVolunteeringPost.getData();
        VolunteeringPostDTO expected = new VolunteeringPostDTO(volunteeringPostId, postTitle, postDescription, volunteeringPostDTO.getPostedTime(), volunteeringPostDTO.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, new HashSet<>());
        assertEquals(expected, volunteeringPostDTO);

        assertFalse(isVolunteer(bobId, bobToken, volunteeringId));

        postService.joinVolunteeringRequest(bobToken, volunteeringPostId, bobId, "Please please please");
        String newRequestMessage = "BobTheBuilder has requested to join volunteering Volunteering";
        assertMessages(aliceId, aliceToken, List.of(newRequestMessage), 1);

        volunteeringService.acceptUserJoinRequest(aliceToken, aliceId, volunteeringId, bobId, 0);
        String acceptedMessage = "You have been accepted to volunteering Volunteering.";
        assertMessages(bobId, bobToken, List.of(acceptedMessage), 1);

        assertTrue(isVolunteer(bobId, bobToken, volunteeringId));
    }

    @Test
    void givenPostAndDenyRequest_whenGetVolunteeringPostAndJoinVolunteering_thenDoNotAddVolunteer() {
        when(gemini.sendQuery(anyString())).thenReturn("");

        createVolunteeringPostByAlice();

        Response<VolunteeringPostDTO> getVolunteeringPost = postService.getVolunteeringPost(bobToken, volunteeringPostId, bobId);
        VolunteeringPostDTO volunteeringPostDTO = getVolunteeringPost.getData();
        VolunteeringPostDTO expected = new VolunteeringPostDTO(volunteeringPostId, postTitle, postDescription, volunteeringPostDTO.getPostedTime(), volunteeringPostDTO.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, new HashSet<>());
        assertEquals(expected, volunteeringPostDTO);

        assertFalse(isVolunteer(bobId, bobToken, volunteeringId));

        postService.joinVolunteeringRequest(bobToken, volunteeringPostId, bobId, "Please please please");
        String newRequestMessage = "BobTheBuilder has requested to join volunteering Volunteering";
        assertMessages(aliceId, aliceToken, List.of(newRequestMessage), 1);

        volunteeringService.denyUserJoinRequest(aliceToken, aliceId, volunteeringId, bobId);
        String acceptedMessage = "You have been denied from volunteering Volunteering.";
        assertMessages(bobId, bobToken, List.of(acceptedMessage), 1);

        assertFalse(isVolunteer(bobId, bobToken, volunteeringId));
    }

    @Test
    void givenNonExistingPost_whenGetVolunteeringPostAndJoinVolunteering_thenError() {
        when(gemini.sendQuery(anyString())).thenReturn("");

        Response<VolunteeringPostDTO> getVolunteeringPost = postService.getVolunteeringPost(bobToken, volunteeringPostId, bobId);
        String expectedError = PostErrors.makePostIdDoesNotExistError(volunteeringPostId);
        assertTrue(getVolunteeringPost.getError());
        assertEquals(expectedError, getVolunteeringPost.getErrorString());

        Response<Boolean> requestRes = postService.joinVolunteeringRequest(bobToken, volunteeringPostId, bobId, "Please please please");
        assertTrue(requestRes.getError());
        assertEquals(expectedError, requestRes.getErrorString());

        Response<String> acceptRes = volunteeringService.acceptUserJoinRequest(aliceToken, aliceId, volunteeringId, bobId, 0);
        expectedError = "Volunteering with id -1 does not exist";
        assertTrue(acceptRes.getError());
        assertEquals(expectedError, acceptRes.getErrorString());
    }

    @Test
    void givenDoubleJoinRequest_whenGetVolunteeringPostAndJoinVolunteering_thenError() {
        when(gemini.sendQuery(anyString())).thenReturn("");

        createVolunteeringPostByAlice();

        Response<VolunteeringPostDTO> getVolunteeringPost = postService.getVolunteeringPost(bobToken, volunteeringPostId, bobId);
        VolunteeringPostDTO volunteeringPostDTO = getVolunteeringPost.getData();
        VolunteeringPostDTO expected = new VolunteeringPostDTO(volunteeringPostId, postTitle, postDescription, volunteeringPostDTO.getPostedTime(), volunteeringPostDTO.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, new HashSet<>());
        assertEquals(expected, volunteeringPostDTO);

        assertFalse(isVolunteer(bobId, bobToken, volunteeringId));

        postService.joinVolunteeringRequest(bobToken, volunteeringPostId, bobId, "Please please please");
        String newVolMessage = "A new volunteering \"Volunteering\" was added to your organization \"Organization\".";
        String newPostMessage = "The new post \"Post\" was created for the volunteering \"Volunteering\" in your organization \"Organization\".";
        String newRequestMessage = "BobTheBuilder has requested to join volunteering Volunteering";
        assertMessages(aliceId, aliceToken, List.of(newVolMessage, newPostMessage, newRequestMessage), -1);

        volunteeringService.acceptUserJoinRequest(aliceToken, aliceId, volunteeringId, bobId, 0);
        String acceptedMessage = "You have been accepted to volunteering Volunteering.";
        assertMessages(bobId, bobToken, List.of(acceptedMessage), -1);

        assertTrue(isVolunteer(bobId, bobToken, volunteeringId));

        Response<Boolean> secondRequestRes = postService.joinVolunteeringRequest(bobToken, volunteeringPostId, bobId, "Please please please");
        String expectedError = "User " + bobId + " is already a volunteer in volunteering " + volunteeringId;
        assertTrue(secondRequestRes.getError());
        assertEquals(expectedError, secondRequestRes.getErrorString());
        assertMessages(aliceId, aliceToken, List.of(newVolMessage, newPostMessage, newRequestMessage), -1);
    }

    @Test
    void givenJoinRequestByManager_whenGetVolunteeringPostAndJoinVolunteering_thenError() {
        when(gemini.sendQuery(anyString())).thenReturn("");

        createVolunteeringPostByAlice();
        makeBobManager();

        Response<VolunteeringPostDTO> getVolunteeringPost = postService.getVolunteeringPost(bobToken, volunteeringPostId, bobId);
        VolunteeringPostDTO volunteeringPostDTO = getVolunteeringPost.getData();
        VolunteeringPostDTO expected = new VolunteeringPostDTO(volunteeringPostId, postTitle, postDescription, volunteeringPostDTO.getPostedTime(), volunteeringPostDTO.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, new HashSet<>());
        assertEquals(expected, volunteeringPostDTO);

        Response<Boolean> isManagerRes = organizationService.isManager(bobToken, bobId, organizationId);
        assertTrue(isManagerRes.getData());
        assertFalse(isVolunteer(bobId, bobToken, volunteeringId));

        Response<Boolean> joinRes = postService.joinVolunteeringRequest(bobToken, volunteeringPostId, bobId, "Please please please");
        String expectedError = "User " + bobId + " is already a manager in organization of volunteering " + volunteeringId;
        assertTrue(joinRes.getError());
        assertEquals(expectedError, joinRes.getErrorString());

        assertFalse(isVolunteer(bobId, bobToken, volunteeringId));
    }

    private void mockVolunteerPostAI(int start) throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("skills", List.of("skill" + start, "skill" + (start + 1)));
        map.put("categories", List.of("cat" + start, "cat" + (start + 1)));
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(map);

        when(gemini.sendQuery(anyString()))
                .thenReturn(String.format("keyword%d, keyword%d, keyword%d, keyword%d", start, start + 1, start + 2, start + 3))
                .thenReturn(jsonString);
    }

    private static Stream<Arguments> validVolunteerPostInputs() {
        return Stream.of(
                Arguments.of("Help with Animals", "We need volunteers to care for rescued dogs."),
                Arguments.of("Food Drive", "Join us in organizing a local food drive event."),
                Arguments.of("Beach Cleanup", "Help clean up the beach and protect marine life."),
                Arguments.of("Tutoring Kids", "Volunteer to tutor kids in math and reading."),
                Arguments.of("Art Therapy", "Support art therapy workshops for mental health.")
        );
    }

    //
    @ParameterizedTest
    @MethodSource("validVolunteerPostInputs")
    void givenValidFields_whenCreateVolunteerPost_thenCreatePost(String title, String description) throws JsonProcessingException {
        mockVolunteerPostAI(1);

        Response<List<VolunteerPostDTO>> allVolunteerPostsRes = postService.getAllVolunteerPosts(aliceToken, aliceId);
        List<VolunteerPostDTO> allVolunteerPosts = allVolunteerPostsRes.getData();
        assertEquals(new ArrayList<>(), allVolunteerPosts);

        Response<Integer> postRes = postService.createVolunteerPost(aliceToken, aliceId, title, description);
        assertFalse(postRes.getError());

        allVolunteerPostsRes = postService.getAllVolunteerPosts(aliceToken, aliceId);
        allVolunteerPosts = allVolunteerPostsRes.getData();
        assertEquals(1, allVolunteerPosts.size());
        VolunteerPostDTO result = allVolunteerPosts.get(0);
        VolunteerPostDTO expected = new VolunteerPostDTO(postRes.getData(), title, description, result.getPostedTime(), result.getLastEditedTime(), aliceId, 0, List.of(aliceId), List.of(), Set.of("keyword1", "keyword2", "keyword3", "keyword4"), List.of("skill1", "skill2"), List.of("cat1", "cat2"));
        assertEquals(expected, result);
    }

    @Test
    void givenInvalidFields_whenCreateVolunteerPost_thenError() throws JsonProcessingException {
        mockVolunteerPostAI(1);

        Response<List<VolunteerPostDTO>> allVolunteerPostsRes = postService.getAllVolunteerPosts(aliceToken, aliceId);
        List<VolunteerPostDTO> allVolunteerPosts = allVolunteerPostsRes.getData();
        assertEquals(new ArrayList<>(), allVolunteerPosts);

        Response<Integer> postRes = postService.createVolunteerPost(aliceToken, aliceId, "", "");
        assertTrue(postRes.getError());
        assertEquals("Invalid post title: .\nInvalid post description: .\n", postRes.getErrorString());

        allVolunteerPostsRes = postService.getAllVolunteerPosts(aliceToken, aliceId);
        allVolunteerPosts = allVolunteerPostsRes.getData();
        assertEquals(new ArrayList<>(), allVolunteerPosts);
    }

    private static Stream<Arguments> validVolunteeringPostInputs() {
        return Stream.of(
                Arguments.of("Help with Animals", "We need volunteers to care for rescued dogs."),
                Arguments.of("Food Drive", "Join us in organizing a local food drive event."),
                Arguments.of("Beach Cleanup", "Help clean up the beach and protect marine life."),
                Arguments.of("Tutoring Kids", "Volunteer to tutor kids in math and reading."),
                Arguments.of("Art Therapy", "Support art therapy workshops for mental health.")
        );
    }

    // 4.2
    @ParameterizedTest
    @MethodSource("validVolunteeringPostInputs")
    void givenValidFieldsAndFounder_whenCreateVolunteeringPost_thenCreate(String title, String description) {
        when(gemini.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");

        createVolunteeringPostByAlice();
        userService.updateUserSkills(bobToken, bobId, List.of("keyword1"));

        int numPostsBefore = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();

        Response<Integer> postIdRes = postService.createVolunteeringPost(aliceToken, title, description, aliceId, volunteeringId);

        int postId = postIdRes.getData();
        Response<VolunteeringPostDTO> postDTORes = postService.getVolunteeringPost(aliceToken, postId, aliceId);
        VolunteeringPostDTO postDTO = postDTORes.getData();
        VolunteeringPostDTO expected = new VolunteeringPostDTO(postId, title, description, postDTO.getPostedTime(), postDTO.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, Set.of("keyword1, keyword2, keyword3, keyword4"));
        assertEquals(expected, postDTO);

        String newPostMessage = String.format("The new post \"%s\" might be relevant for you!", title);
        String managersMessage = String.format("The new post \"%s\" was created for the volunteering \"%s\" in your organization \"%s\".", title, "Volunteering", "Organization");

        assertMessages(aliceId, aliceToken, List.of(managersMessage), 1);
        assertMessages(bobId, bobToken, List.of(newPostMessage), 1);

        int numPostsAfter = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();
        assertEquals(1, numPostsAfter - numPostsBefore);
    }

    @ParameterizedTest
    @MethodSource("validVolunteeringPostInputs")
    void givenValidFieldsAndManager_whenCreateVolunteeringPost_thenCreate(String title, String description) {
        when(gemini.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");

        createVolunteeringPostByAlice();
        makeBobManager();

        userService.updateUserSkills(bobToken, bobId, List.of("keyword1"));

        int numPostsBefore = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();

        Response<Integer> postIdRes = postService.createVolunteeringPost(aliceToken, title, description, aliceId, volunteeringId);

        int postId = postIdRes.getData();
        Response<VolunteeringPostDTO> postDTORes = postService.getVolunteeringPost(aliceToken, postId, aliceId);
        VolunteeringPostDTO postDTO = postDTORes.getData();
        VolunteeringPostDTO expected = new VolunteeringPostDTO(postId, title, description, postDTO.getPostedTime(), postDTO.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, Set.of("keyword1, keyword2, keyword3, keyword4"));
        assertEquals(expected, postDTO);

        String newPostMessage = String.format("The new post \"%s\" might be relevant for you!", title);
        String managersMessage = String.format("The new post \"%s\" was created for the volunteering \"%s\" in your organization \"%s\".", title, "Volunteering", "Organization");

        assertMessages(bobId, bobToken, List.of(managersMessage, newPostMessage), 2);
        assertMessages(aliceId, aliceToken, List.of(managersMessage), 1);

        int numPostsAfter = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();
        assertEquals(1, numPostsAfter - numPostsBefore);
    }

    @Test
    void givenNonExistingUser_whenCreateVolunteeringPost_thenError() {
        int numPostsBefore = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();

        Response<Integer> postIdRes = postService.createVolunteeringPost(aliceToken, postTitle, postDescription, "newUser", volunteeringId);
        assertTrue(postIdRes.getError());
        assertEquals("Invalid token", postIdRes.getErrorString());

        verify(gemini, times(0)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        int numPostsAfter = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();
        assertEquals(0, numPostsAfter - numPostsBefore);
    }

    @Test
    void givenNonManager_whenCreateVolunteeringPost_thenError() {
        createVolunteeringPostByAlice();
        reset(gemini);
        reset(notificationSocketSender);

        int numPostsBefore = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();

        Response<Integer> postIdRes = postService.createVolunteeringPost(bobToken, postTitle, postDescription, bobId, volunteeringId);
        assertTrue(postIdRes.getError());
        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(bobId, "Organization", "post about the organization's volunteering"), postIdRes.getErrorString());

        verify(gemini, times(0)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        int numPostsAfter = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();
        assertEquals(0, numPostsAfter - numPostsBefore);
    }

    @Test
    void givenInvalidFields_whenCreateVolunteeringPost_thenError() {
        createVolunteeringPostByAlice();
        reset(gemini);
        reset(notificationSocketSender);

        int numPostsBefore = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();

        Response<Integer> postIdRes = postService.createVolunteeringPost(aliceToken, "", postDescription, aliceId, volunteeringId);
        assertTrue(postIdRes.getError());
        assertEquals("Invalid post title: .\n", postIdRes.getErrorString());

        verify(gemini, times(1)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        int numPostsAfter = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();
        assertEquals(0, numPostsAfter - numPostsBefore);
    }

    @Test
    void givenNonExistingVolunteeringId_whenCreateVolunteeringPost_thenError() {
        int numPostsBefore = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();

        Response<Integer> postIdRes = postService.createVolunteeringPost(aliceToken, postTitle, postDescription, aliceId, volunteeringId);
        assertTrue(postIdRes.getError());
        assertEquals("Volunteering with id " + volunteeringId + " does not exist", postIdRes.getErrorString());

        verify(gemini, times(0)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        int numPostsAfter = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().size();
        assertEquals(0, numPostsAfter - numPostsBefore);
    }

    @ParameterizedTest
    @MethodSource("validVolunteeringPostInputs")
    void givenExistingPostAndValidFieldsByFounder_whenEditVolunteeringPost_thenEdit(String newTitle, String newDescription) {
        when(gemini.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");
        createVolunteeringPostByAlice();

        VolunteeringPostDTO postBefore = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId).getData();
        VolunteeringPostDTO expectedBefore = new VolunteeringPostDTO(volunteeringPostId, postTitle, postDescription, postBefore.getPostedTime(), postBefore.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, Set.of("keyword1, keyword2, keyword3, keyword4"));
        assertEquals(expectedBefore, postBefore);

        postService.editVolunteeringPost(aliceToken, volunteeringPostId, newTitle, newDescription, aliceId);

        VolunteeringPostDTO postAfter = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId).getData();
        VolunteeringPostDTO expectedAfter = new VolunteeringPostDTO(volunteeringPostId, newTitle, newDescription, postAfter.getPostedTime(), postAfter.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, Set.of("keyword1, keyword2, keyword3, keyword4"));
        assertEquals(expectedAfter, postAfter);

        String message = String.format("The post \"%s\" of the volunteering \"%s\" in your organization \"%s\" was edited.", postTitle, "Volunteering", "Organization");
        assertMessages(aliceId, aliceToken, List.of(message), 1);
    }

    @ParameterizedTest
    @MethodSource("validVolunteeringPostInputs")
    void givenExistingPostAndValidFieldsByManager_whenEditVolunteeringPost_thenEdit(String newTitle, String newDescription) {
        when(gemini.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");
        createVolunteeringPostByAlice();
        makeBobManager();

        VolunteeringPostDTO postBefore = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId).getData();
        VolunteeringPostDTO expectedBefore = new VolunteeringPostDTO(volunteeringPostId, postTitle, postDescription, postBefore.getPostedTime(), postBefore.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, Set.of("keyword1, keyword2, keyword3, keyword4"));
        assertEquals(expectedBefore, postBefore);

        postService.editVolunteeringPost(bobToken, volunteeringPostId, newTitle, newDescription, bobId);

        VolunteeringPostDTO postAfter = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId).getData();
        VolunteeringPostDTO expectedAfter = new VolunteeringPostDTO(volunteeringPostId, newTitle, newDescription, postAfter.getPostedTime(), postAfter.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, Set.of("keyword1, keyword2, keyword3, keyword4"));
        assertEquals(expectedAfter, postAfter);

        String message = String.format("The post \"%s\" of the volunteering \"%s\" in your organization \"%s\" was edited.", postTitle, "Volunteering", "Organization");
        assertMessages(aliceId, aliceToken, List.of(message), 1);
        assertMessages(bobId, bobToken, List.of(message), 1);
    }

    @Test
    void givenNonExistingUser_whenEditVolunteeringPost_thenThrowException() {
        when(gemini.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");
        createVolunteeringPostByAlice();
        reset(gemini);
        reset(notificationSocketSender);

        VolunteeringPostDTO postBefore = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId).getData();
        VolunteeringPostDTO expectedBefore = new VolunteeringPostDTO(volunteeringPostId, postTitle, postDescription, postBefore.getPostedTime(), postBefore.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, Set.of("keyword1, keyword2, keyword3, keyword4"));
        assertEquals(expectedBefore, postBefore);

        Response<Boolean> editRes = postService.editVolunteeringPost(aliceToken, volunteeringPostId, "newTitle", "newDescription", "newUser");
        assertTrue(editRes.getError());
        assertEquals("Invalid token", editRes.getErrorString());

        VolunteeringPostDTO postAfter = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId).getData();
        assertEquals(expectedBefore, postAfter);

        verify(gemini, times(0)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonValidFields_whenEditVolunteeringPost_thenThrowException() {
        when(gemini.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");
        createVolunteeringPostByAlice();
        reset(gemini);
        reset(notificationSocketSender);

        VolunteeringPostDTO postBefore = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId).getData();
        VolunteeringPostDTO expectedBefore = new VolunteeringPostDTO(volunteeringPostId, postTitle, postDescription, postBefore.getPostedTime(), postBefore.getLastEditedTime(), aliceId, 0, volunteeringId, organizationId, 0, Set.of("keyword1, keyword2, keyword3, keyword4"));
        assertEquals(expectedBefore, postBefore);

        Response<Boolean> editRes = postService.editVolunteeringPost(aliceToken, volunteeringPostId, "", "", "newUser");
        assertTrue(editRes.getError());
        assertEquals("Invalid token", editRes.getErrorString());

        VolunteeringPostDTO postAfter = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId).getData();
        assertEquals(expectedBefore, postAfter);

        verify(gemini, times(0)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingPost_whenEditVolunteeringPost_thenThrowException() {
        Response<Boolean> editRes = postService.editVolunteeringPost(aliceToken, volunteeringPostId, "newPostTitle", "newPostDescription", aliceId);
        assertTrue(editRes.getError());
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteeringPostId), editRes.getErrorString());

        verify(gemini, times(0)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonManager_whenEditVolunteeringPost_thenThrowException() {
        when(gemini.sendQuery(anyString())).thenReturn("keyword1, keyword2, keyword3, keyword4");
        createVolunteeringPostByAlice();
        reset(gemini);
        reset(notificationSocketSender);

        Response<Boolean> editRes = postService.editVolunteeringPost(bobToken, volunteeringPostId, "newPostTitle", "newPostDescription", bobId);
        assertTrue(editRes.getError());
        assertEquals(PostErrors.makeUserIsNotAllowedToMakePostActionError(postTitle, bobId, "edit"), editRes.getErrorString());

        verify(gemini, times(0)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @ParameterizedTest
    @MethodSource("validVolunteerPostInputs")
    void givenExistingPostAndValidFieldsByPoster_whenEditVolunteerPost_thenEdit(String newTitle, String newDescription) throws JsonProcessingException {
        mockVolunteerPostAI(1);
        createVolunteerPostByAliceAndAddBob();
        reset(gemini);
        reset(notificationSocketSender);

        VolunteerPostDTO postBefore = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData();
        VolunteerPostDTO expectedBefore = new VolunteerPostDTO(volunteerPostId, postTitle, postDescription, postBefore.getPostedTime(), postBefore.getLastEditedTime(), aliceId, 0, List.of(aliceId, bobId), List.of(), Set.of("keyword1", "keyword2", "keyword3", "keyword4"), List.of("skill1", "skill2"), List.of("cat1", "cat2"));
        assertEquals(expectedBefore, postBefore);

        mockVolunteerPostAI(4);
        postService.editVolunteerPost(aliceToken, aliceId, volunteerPostId, newTitle, newDescription);

        VolunteerPostDTO postAfter = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData();
        VolunteerPostDTO expectedAfter = new VolunteerPostDTO(volunteerPostId, newTitle, newDescription, postAfter.getPostedTime(), postAfter.getLastEditedTime(), aliceId, 0, List.of(aliceId, bobId), List.of(), Set.of("keyword4", "keyword5", "keyword6", "keyword7"), List.of("skill4", "skill5"), List.of("cat4", "cat5"));
        assertEquals(expectedAfter, postAfter);

        verify(gemini, times(2)).sendQuery(Mockito.anyString());

        String message = String.format("Your post \"%s\" was %s.", postTitle, "edited");
        assertMessages(aliceId, aliceToken, List.of(message), 1);
        assertMessages(bobId, bobToken, List.of(message), 1);
    }

    @Test
    void givenNonExistingUser_whenEditVolunteerPost_thenThrowException() throws JsonProcessingException {
        mockVolunteerPostAI(1);
        createVolunteerPostByAliceAndAddBob();
        reset(gemini);
        reset(notificationSocketSender);

        VolunteerPostDTO postBefore = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData();
        VolunteerPostDTO expectedBefore = new VolunteerPostDTO(volunteerPostId, postTitle, postDescription, postBefore.getPostedTime(), postBefore.getLastEditedTime(), aliceId, 0, List.of(aliceId, bobId), List.of(), Set.of("keyword1", "keyword2", "keyword3", "keyword4"), List.of("skill1", "skill2"), List.of("cat1", "cat2"));
        assertEquals(expectedBefore, postBefore);

        Response<Boolean> editRes = postService.editVolunteerPost(aliceToken, "newUser", volunteerPostId, "newTitle", "newDesc");
        assertTrue(editRes.getError());
        assertEquals("Invalid token", editRes.getErrorString());

        VolunteerPostDTO postAfter = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData();
        assertEquals(expectedBefore, postAfter);

        verify(gemini, times(0)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonValidFields_whenEditVolunteerPost_thenThrowException() throws JsonProcessingException {
        mockVolunteerPostAI(1);
        createVolunteerPostByAliceAndAddBob();
        reset(gemini);
        reset(notificationSocketSender);

        VolunteerPostDTO postBefore = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData();
        VolunteerPostDTO expectedBefore = new VolunteerPostDTO(volunteerPostId, postTitle, postDescription, postBefore.getPostedTime(), postBefore.getLastEditedTime(), aliceId, 0, List.of(aliceId, bobId), List.of(), Set.of("keyword1", "keyword2", "keyword3", "keyword4"), List.of("skill1", "skill2"), List.of("cat1", "cat2"));
        assertEquals(expectedBefore, postBefore);

        Response<Boolean> editRes = postService.editVolunteerPost(aliceToken, aliceId, volunteerPostId, "", "");
        assertTrue(editRes.getError());
        assertEquals("Invalid post title: .\n" + "Invalid post description: .\n", editRes.getErrorString());

        VolunteerPostDTO postAfter = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData();
        assertEquals(expectedBefore, postAfter);

        verify(gemini, times(2)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingPost_whenEditVolunteerPost_thenThrowException() {
        Response<Boolean> editRes = postService.editVolunteerPost(aliceToken, aliceId, volunteerPostId, "newPostTitle", "newPostDescription");
        assertTrue(editRes.getError());
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteeringPostId), editRes.getErrorString());

        verify(gemini, times(0)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonPoster_whenEditVolunteerPost_thenThrowException() throws JsonProcessingException {
        mockVolunteerPostAI(1);
        createVolunteerPostByAliceAndAddBob();
        reset(gemini);
        reset(notificationSocketSender);

        Response<Boolean> editRes = postService.editVolunteerPost(bobToken, bobId, volunteerPostId, "newPostTitle", "newPostDescription");
        assertTrue(editRes.getError());
        assertEquals(PostErrors.makeUserIsNotAllowedToMakePostActionError(postTitle, bobId, "edit"), editRes.getErrorString());

        verify(gemini, times(0)).sendQuery(Mockito.anyString());
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenExistingPostByFounder_whenRemoveVolunteeringPost_thenRemove() {
        createVolunteeringPostByAlice();

        Response<VolunteeringPostDTO> getPostRes = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId);
        assertFalse(getPostRes.getError());

        postService.removeVolunteeringPost(aliceToken, volunteeringPostId, aliceId);

        getPostRes = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId);
        assertTrue(getPostRes.getError());

        Response<List<ReportDTO>> getReportsRes = reportService.getAllVolunteeringPostReports(adminToken, adminId);
        assertEquals(new ArrayList<>(), getReportsRes.getData());

        String message = String.format("The post \"%s\" of the volunteering \"%s\" in your organization \"%s\" was removed", postTitle, "Volunteering", "Organization");
        assertMessages(aliceId, aliceToken, List.of(message), 1);
    }

    @Test
    void givenNonExistingUser_whenRemoveVolunteeringPost_thenThrowException() {
        createVolunteeringPostByAlice();
        reset(notificationSocketSender);

        Response<VolunteeringPostDTO> getPostRes = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId);
        assertFalse(getPostRes.getError());

        Response<Boolean> removeRes = postService.removeVolunteeringPost(aliceToken, volunteeringPostId, "newuser");
        assertEquals("Invalid token", removeRes.getErrorString());

        getPostRes = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId);
        assertFalse(getPostRes.getError());

        verifyNoInteractions(reportService);
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingPost_whenRemoveVolunteeringPost_thenThrowException() {
        Response<VolunteeringPostDTO> getPostRes = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId);
        assertTrue(getPostRes.getError());

        Response<Boolean> removeRes = postService.removeVolunteeringPost(aliceToken, volunteeringPostId, aliceId);
        assertTrue(removeRes.getError());
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteeringPostId), removeRes.getErrorString());

        verifyNoInteractions(reportService);
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonPoster_whenRemoveVolunteeringPost_thenThrowException() {
        createVolunteeringPostByAlice();
        reset(notificationSocketSender);

        Response<VolunteeringPostDTO> getPostRes = postService.getVolunteeringPost(aliceToken, volunteeringPostId, aliceId);
        assertFalse(getPostRes.getError());

        Response<Boolean> removeRes = postService.removeVolunteeringPost(bobToken, volunteeringPostId, bobId);
        assertTrue(removeRes.getError());
        assertEquals(PostErrors.makeUserIsNotAllowedToMakePostActionError(postTitle, bobId, "remove"), removeRes.getErrorString());

        verifyNoInteractions(reportService);
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenExistingPostByPoster_whenRemoveVolunteerPost_thenRemove() {
        createVolunteerPostByAliceAndAddBob();
        Response<VolunteerPostDTO> getPostRes = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId);
        assertFalse(getPostRes.getError());

        postService.removeVolunteerPost(aliceToken, aliceId, volunteerPostId);

        getPostRes = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId);
        assertTrue(getPostRes.getError());
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId), getPostRes.getErrorString());

        Response<List<ReportDTO>> getReportsRes = reportService.getAllVolunteerPostReports(adminToken, adminId);
        assertEquals(new ArrayList<>(), getReportsRes.getData());

        String message = String.format("Your post \"%s\" was %s.", postTitle, "removed");
        assertMessages(aliceId, aliceToken, List.of(message), 1);
        assertMessages(bobId, bobToken, List.of(message), 1);
    }

    @Test
    void givenNonExistingUser_whenRemoveVolunteerPost_thenThrowException() {
        createVolunteerPostByAliceAndAddBob();
        reset(notificationSocketSender);

        Response<VolunteerPostDTO> getPostRes = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId);
        assertFalse(getPostRes.getError());

        Response<Boolean> removeRes = postService.removeVolunteerPost(bobToken, "newUser", volunteerPostId);
        assertTrue(removeRes.getError());
        assertEquals("Invalid token", removeRes.getErrorString());

        getPostRes = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId);
        assertFalse(getPostRes.getError());

        verifyNoInteractions(reportService);
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingPost_whenRemoveVolunteerPost_thenThrowException() {
        Response<VolunteerPostDTO> getPostRes = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId);
        assertTrue(getPostRes.getError());

        Response<Boolean> removeRes = postService.removeVolunteerPost(aliceToken, aliceId, volunteerPostId);
        assertTrue(removeRes.getError());
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId), removeRes.getErrorString());

        verifyNoInteractions(reportService);
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonPoster_whenRemoveVolunteerPost_thenThrowException() {
        createVolunteerPostByAliceAndAddBob();
        reset(notificationSocketSender);

        Response<VolunteerPostDTO> getPostRes = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId);
        assertFalse(getPostRes.getError());

        Response<Boolean> removeRes = postService.removeVolunteerPost(bobToken, bobId, volunteerPostId);
        assertTrue(removeRes.getError());
        assertEquals(PostErrors.makeUserIsNotAllowedToMakePostActionError(postTitle, bobId, "remove"), removeRes.getErrorString());

        verifyNoInteractions(reportService);
        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        getPostRes = postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId);
        assertFalse(getPostRes.getError());
    }

    @Test
    void givenPosterToExistingUserAndExistingPostAndAccept_whenAddRelatedUser_thenAddedUser() {
        createVolunteerPostByAlice();
        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains(bobId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", bobId, "volunteer post", volunteerPostId), exception.getMessage());

        postService.sendAddRelatedUserRequest(aliceToken, aliceId, volunteerPostId, bobId);

        assertDoesNotThrow(() -> facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST));

        String requestMessage = String.format("%s asked you to join the volunteer post \"%s\".", aliceId, postTitle);
        assertMessages(bobId ,bobToken, List.of(requestMessage), 1);

        postService.handleAddRelatedUserRequest(bobToken, bobId, volunteerPostId, true);
        assertTrue(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains(bobId));

        String approvedMessage = String.format("%s has approved your request to join the post \"%s\".", bobId, postTitle);
        assertMessages(aliceId ,aliceToken, List.of(approvedMessage), 1);
    }

    @Test
    void givenPosterToExistingUserAndExistingPostAndDeny_whenAddRelatedUser_thenNoAddedUser() {
        createVolunteerPostByAlice();
        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains(bobId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", bobId, "volunteer post", volunteerPostId), exception.getMessage());

        postService.sendAddRelatedUserRequest(aliceToken, aliceId, volunteerPostId, bobId);

        assertDoesNotThrow(() -> facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST));

        String requestMessage = String.format("%s asked you to join the volunteer post \"%s\".", aliceId, postTitle);
        assertMessages(bobId ,bobToken, List.of(requestMessage), 1);

        postService.handleAddRelatedUserRequest(bobToken, bobId, volunteerPostId, false);
        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains(bobId));

        String approvedMessage = String.format("%s has denied your request to join the post \"%s\".", bobId, postTitle);
        assertMessages(aliceId ,aliceToken, List.of(approvedMessage), 1);
    }

    @Test
    void givenNonExistingAssigner_whenSendAddRelatedUserRequest_thenThrowException() {
        createVolunteerPostByAlice();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", bobId, "volunteer post", volunteerPostId), exception.getMessage());
        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains(bobId));

        Response<Boolean> requestRes = postService.sendAddRelatedUserRequest(bobToken, "newUser", volunteerPostId, bobId);
        assertTrue(requestRes.getError());
        assertEquals("Invalid token", requestRes.getErrorString());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", bobId, "volunteer post", volunteerPostId), exception.getMessage());
        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains(bobId));

        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains(bobId));
    }

    @Test
    void givenNonExistingAssignee_whenSendAddRelatedUserRequest_thenThrowException() {
        createVolunteerPostByAlice();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            facadeManager.getPostsFacade().getRequestRepository().getRequest("newUser", volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", "newUser", "volunteer post", volunteerPostId), exception.getMessage());
        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains("newUser"));

        Response<Boolean> requestRes = postService.sendAddRelatedUserRequest(aliceToken, aliceId, volunteerPostId, "newUser");
        assertTrue(requestRes.getError());
        assertEquals("User newUser doesn't exist", requestRes.getErrorString());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            facadeManager.getPostsFacade().getRequestRepository().getRequest("newUser", volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", "newUser", "volunteer post", volunteerPostId), exception.getMessage());
        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains("newUser"));

        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingPost_whenSendAddRelatedUserRequest_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", bobId, "volunteer post", volunteerPostId), exception.getMessage());

        Response<Boolean> requestRes = postService.sendAddRelatedUserRequest(aliceToken, aliceId, volunteerPostId, bobId);
        assertTrue(requestRes.getError());
        assertEquals(PostErrors.makePostIdDoesNotExistError(volunteerPostId), requestRes.getErrorString());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", bobId, "volunteer post", volunteerPostId), exception.getMessage());

        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenExistingRequest_whenSendAddRelatedUserRequest_thenThrowException() {
        createVolunteerPostByAlice();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", bobId, "volunteer post", volunteerPostId), exception.getMessage());

        postService.sendAddRelatedUserRequest(aliceToken, aliceId, volunteerPostId, bobId);
        String message = String.format("%s asked you to join the volunteer post \"%s\".", aliceId, postTitle);
        assertMessages(bobId, bobToken, List.of(message), 1);
        assertDoesNotThrow(() -> facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST));

        reset(notificationSocketSender);

        Response<Boolean> requestRes = postService.sendAddRelatedUserRequest(aliceToken, aliceId, volunteerPostId, bobId);
        assertTrue(requestRes.getError());
        assertEquals(String.format("A request to assign %s to %s with id %d already exists.", bobId, "volunteer post", volunteerPostId), requestRes.getErrorString());

        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenRelatedUser_whenSendAddRelatedUserRequest_thenThrowException() {
        createVolunteerPostByAliceAndAddBob();
        reset(notificationSocketSender);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", bobId, "volunteer post", volunteerPostId), exception.getMessage());

        Response<Boolean> requestRes = postService.sendAddRelatedUserRequest(aliceToken, aliceId, volunteerPostId, bobId);
        assertTrue(requestRes.getError());
        assertEquals("User " + bobId + " is already a related user of the post " + postTitle + ".", requestRes.getErrorString());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            facadeManager.getPostsFacade().getRequestRepository().getRequest(bobId, volunteerPostId, RequestObject.VOLUNTEER_POST);
        });
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", bobId, "volunteer post", volunteerPostId), exception.getMessage());

        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingUser_whenHandleAddRelatedUserRequest_thenThrowException() {
        createVolunteerPostByAlice();
        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains("newUser"));

        Response<Boolean> handleRes = postService.handleAddRelatedUserRequest(aliceToken, "newUser", volunteerPostId, true);
        assertTrue(handleRes.getError());
        assertEquals("Invalid token", handleRes.getErrorString());

        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains("newUser"));

        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingRequest_whenHandleAddRelatedUserRequest_thenThrowException() {
        createVolunteerPostByAlice();
        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains(bobId));

        Response<Boolean> handleRes = postService.handleAddRelatedUserRequest(bobToken, bobId, volunteerPostId, true);
        assertTrue(handleRes.getError());
        assertEquals(String.format("A request to assign %s to %s with id %d does not exist.", bobId, "volunteer post", volunteerPostId), handleRes.getErrorString());

        assertFalse(postService.getVolunteerPost(aliceToken, volunteerPostId, aliceId).getData().getRelatedUsers().contains("newUser"));

        verify(notificationSocketSender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void whenSearchSortFilterPosts_thenReturnRelevantPosts() {
        Response<Integer> createOrganization = organizationService.createOrganization(aliceToken, "Organization", "Description", "052-0520520", "organization@manager.com", aliceId);
        this.organizationId = createOrganization.getData();
        Response<Integer> createVolunteering = organizationService.createVolunteering(aliceToken, organizationId, "Volunteering1", "Description", aliceId);
        int volunteeringId1 = createVolunteering.getData();
        volunteeringService.updateVolunteeringSkills(aliceToken, aliceId, volunteeringId1, List.of("s1", "s2", "s3"));
        createVolunteering = organizationService.createVolunteering(aliceToken, organizationId, "Volunteering2", "Description", aliceId);
        int volunteeringId2 = createVolunteering.getData();
        volunteeringService.updateVolunteeringSkills(aliceToken, aliceId, volunteeringId2, List.of("s3", "s4", "s5"));
        volunteeringService.addVolunteeringLocation(aliceToken, aliceId, volunteeringId2, "loc1", new AddressTuple("Beer Sheva", "", ""));

        userService.updateUserSkills(aliceToken, aliceId, List.of("s1", "s4"));
        userService.updateUserPreferences(aliceToken, aliceId, List.of("s2"));
        userService.updateUserPreferences(bobToken, bobId, List.of("s5"));

        when(gemini.sendQuery(anyString()))
                .thenReturn(
                        "beach, cleanup, environment, community, volunteer",
                        "food, distribution, community, help, volunteer",
                        "trees, planting, environment, volunteer, outdoors",
                        "animals, shelter, care, help, volunteer",
                        "technology, teaching, senior citizens, community,help",
                        "gardening, community, sustainability, environment, volunteer",
                        "library, books, education, community, organization",
                        "park, beautification, environment, community, cleaning",
                        "recycling, education, community, environment, awareness",
                        "marathon, event, support, community, volunteer");

        int id1 = postService.createVolunteeringPost(aliceToken, "Beach Cleanup Drive", "Join us in cleaning up the local beach and making it safe and beautiful for everyone.", aliceId, volunteeringId1).getData();
        int id2 = postService.createVolunteeringPost(aliceToken, "Food Bank Assistance", "Help organize, pack, and distribute food to families in need at the city food bank.", aliceId, volunteeringId2).getData();
        int id3 = postService.createVolunteeringPost(aliceToken, "Tree Planting Campaign", "Plant trees in local neighborhoods and contribute to a greener tomorrow.", aliceId, volunteeringId1).getData();
        int id4 = postService.createVolunteeringPost(aliceToken, "Animal Shelter Help", "Assist at the animal shelter by feeding, cleaning, and playing with rescued animals.", aliceId, volunteeringId2).getData();
        int id5 = postService.createVolunteeringPost(aliceToken, "Senior Tech Help", "Help senior citizens learn to use smartphones and computers at our tech help desk.", aliceId, volunteeringId1).getData();
        int id6 = postService.createVolunteeringPost(aliceToken, "Community Garden Project", "Work with local volunteers to create and maintain a sustainable community garden.", aliceId, volunteeringId2).getData();
        int id7 = postService.createVolunteeringPost(aliceToken, "Library Book Sorting", "Sort and organize book donations at the community library to support literacy programs.", aliceId, volunteeringId1).getData();
        int id8 = postService.createVolunteeringPost(aliceToken, "Park Beautification", "Help clean, paint, and decorate the city park to make it more welcoming for families.", aliceId, volunteeringId2).getData();
        int id9 = postService.createVolunteeringPost(aliceToken, "Recycling Awareness Event", "Spread awareness about recycling by helping organize a community education event.", aliceId, volunteeringId1).getData();
        int id10 = postService.createVolunteeringPost(aliceToken, "Local Marathon Support", "Support the local marathon by handing out water, guiding runners, and cheering them on.", aliceId, volunteeringId2).getData();
        List<PostDTO> allPosts = postService.getAllVolunteeringPosts(aliceToken, aliceId).getData().stream().map((post) -> post).collect(Collectors.toList());

        Response<List<? extends PostDTO>> res = postService.searchByKeywords(aliceToken, "environment", aliceId, allPosts, true);
        Set<Integer> search1Ids = res.getData()
                .stream().map(post -> post.getId()).collect(Collectors.toSet());
        assertEquals(Set.of(id1, id3, id6, id8, id9), search1Ids);

        Set<Integer> search2Ids = postService.searchByKeywords(aliceToken, "volunteer", aliceId, allPosts, true).getData()
                .stream().map(post -> post.getId()).collect(Collectors.toSet());
        assertEquals(Set.of(id1, id2, id3, id4, id6, id10), search2Ids);

        Set<Integer> search3Ids = postService.searchByKeywords(aliceToken, "help", aliceId, allPosts, true).getData()
                .stream().map(post -> post.getId()).collect(Collectors.toSet());
        assertEquals(Set.of(id2, id4, id5, id8, id9), search3Ids);

        Set<Integer> search4Ids = postService.searchByKeywords(aliceToken, "community", aliceId, allPosts, true).getData()
                .stream().map(post -> post.getId()).collect(Collectors.toSet());
        assertEquals(Set.of(id1, id2, id5, id6, id7, id8, id9, id10), search4Ids);

        Set<Integer> search5Ids = postService.searchByKeywords(aliceToken, "animals", aliceId, allPosts, true).getData()
                .stream().map(post -> post.getId()).collect(Collectors.toSet());
        assertEquals(Set.of(id4), search5Ids);

        Set<Integer> search6Ids = postService.searchByKeywords(aliceToken, "education", aliceId, allPosts, true).getData()
                .stream().map(post -> post.getId()).collect(Collectors.toSet());
        assertEquals(Set.of(id7, id9), search6Ids);

        Set<Integer> search7Ids = postService.searchByKeywords(aliceToken, "event", aliceId, allPosts, true).getData()
                .stream().map(post -> post.getId()).collect(Collectors.toSet());
        assertEquals(Set.of(id9, id10), search7Ids);

        Set<Integer> search8Ids = postService.searchByKeywords(aliceToken, "books", aliceId, allPosts, true).getData()
                .stream().map(post -> post.getId()).collect(Collectors.toSet());
        assertEquals(Set.of(id7), search8Ids);

        Set<Integer> search9Ids = postService.searchByKeywords(aliceToken, "gardening", aliceId, allPosts, true).getData()
                .stream().map(post -> post.getId()).collect(Collectors.toSet());
        assertEquals(Set.of(id6), search9Ids);

        Set<Integer> search10Ids = postService.searchByKeywords(aliceToken, "tech", aliceId, allPosts, true).getData()
                .stream().map(post -> post.getId()).collect(Collectors.toSet());
        assertEquals(Set.of(id5), search10Ids);

        Set<Integer> filter1Ids = postService.filterVolunteeringPosts(aliceToken, Set.of(), Set.of("s1", "s2"), Set.of(), Set.of("Organization"), Set.of("Volunteering1"), aliceId, search1Ids.stream().toList(), false).getData()
                .stream()
                .map(post -> post.getId())
                .collect(Collectors.toSet());
        assertEquals(Set.of(id1, id3, id9), filter1Ids);

        Set<Integer> filter2Ids = postService.filterVolunteeringPosts(aliceToken, Set.of(), Set.of("s1", "s2"), Set.of(), Set.of(), Set.of("Volunteering2"), aliceId, search1Ids.stream().toList(), false).getData()
                .stream()
                .map(post -> post.getId())
                .collect(Collectors.toSet());
        assertEquals(Set.of(), filter2Ids);

        List<VolunteeringPostDTO> filter3Posts = postService.filterVolunteeringPosts(aliceToken, Set.of(), Set.of("s3", "s5"), Set.of(), Set.of(), Set.of(), aliceId, search1Ids.stream().toList(), false).getData();
        Set<Integer> filter3Ids = filter3Posts
                .stream()
                .map(post -> post.getId())
                .collect(Collectors.toSet());
        assertEquals(Set.of(id1, id3, id6, id8, id9), filter3Ids);

        Set<Integer> filter4Ids = postService.filterVolunteeringPosts(aliceToken, Set.of(), Set.of("s1"), Set.of("Beer Sheva"), Set.of(), Set.of("Volunteering2"), aliceId, search1Ids.stream().toList(), false).getData()
                .stream()
                .map(post -> post.getId())
                .collect(Collectors.toSet());
        assertEquals(Set.of(), filter4Ids);

        List<Integer> aliceSortByRelevance = postService.sortByRelevance(aliceToken, aliceId, filter3Posts).getData().stream().map(post -> post.getId()).collect(Collectors.toList());
        assertEquals(List.of(id1, id3, id9, id6, id8), aliceSortByRelevance);

        List<Integer> bobSortByRelevance = postService.sortByRelevance(bobToken, bobId, filter3Posts).getData().stream().map(post -> post.getId()).collect(Collectors.toList());
        assertEquals(List.of(id6, id8, id1, id3, id9), bobSortByRelevance);

        List<Integer> sortByPostingTime = postService.sortByPostingTime(bobToken, bobId, filter3Posts.stream().collect(Collectors.toList())).getData().stream().map(post -> post.getId()).collect(Collectors.toList());
        assertEquals(List.of(id9, id8, id6, id3, id1), sortByPostingTime);
    }
}
