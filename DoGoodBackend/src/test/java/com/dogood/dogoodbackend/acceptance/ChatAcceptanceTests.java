package com.dogood.dogoodbackend.acceptance;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest;
import com.dogood.dogoodbackend.api.userrequests.VerifyEmailRequest;
import com.dogood.dogoodbackend.domain.chat.Message;
import com.dogood.dogoodbackend.domain.chat.MessageDTO;
import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.users.notificiations.Notification;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.emailverification.VerificationData;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.*;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.dogood.dogoodbackend.utils.PostErrors;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
public class ChatAcceptanceTests {
    @MockitoBean
    ChatSocketSender chatSocketSender;

    //THIS IS IMPORTANT DO IT IN ALL ACCEPTANCE TESTS
    @MockitoBean
    NotificationSocketSender notificationSocketSender;

    //THIS IS ALSO VERY IMPORTANT
    @MockitoBean
    FirebaseMessaging firebaseMessaging;

    //THE INTERNET WILL BREAK IF WE DONT DO THIS IN EVERY ACCEPTANCE TEST
    @MockitoBean
    Gemini gemini;

    @MockitoBean
    EmailSender emailSender;

    @MockitoBean
    VerificationCacheService verificationCacheService;

    @Autowired
    ChatService chatService;
    @Autowired
    UserService userService;
    @Autowired
    PostService postService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    VolunteeringService volunteeringService;

    //jpas for easy reset
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

    private String organizationMangerId;
    private String aliceId;
    private String bobId;

    private String organizationManagerToken;
    private String aliceToken;
    private String bobToken;

    private int organizationId;
    private int volunteeringId;

    @BeforeEach
    void setUp() {
        messageJPA.deleteAll();
        volunteeringJPA.deleteAll();
        organizationJPA.deleteAll();
        notificationJPA.deleteAll();
        userJPA.deleteAll();
        volunteerPostJPA.deleteAll();
        organizationMangerId = "OrgMan";
        aliceId = "Alice";
        bobId = "Bobb";
        userService.register(organizationMangerId,
                "123456",
                "Organization Manager",
                "organization@manager.com",
                "052-0520520",
                new Date(),
                null);
        userService.register(aliceId,
                "123456",
                "Alice Alice",
                "alice@dogood.com",
                "052-0520520",
                new Date(),
                null);
        userService.register(bobId,
                "123456",
                "Bob Bob",
                "bob@dogood.com",
                "052-0520520",
                new Date(),
                null);

        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(organizationMangerId),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail("organization@manager.com");
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
        VerifyEmailRequest orgEmailRequest = new VerifyEmailRequest(organizationMangerId,"");
        VerifyEmailRequest alEmailRequest = new VerifyEmailRequest(aliceId,"");
        VerifyEmailRequest bobEmailRequest = new VerifyEmailRequest(bobId,"");
        userService.verifyEmail(orgEmailRequest);
        userService.verifyEmail(alEmailRequest);
        userService.verifyEmail(bobEmailRequest);

        Response<String> login1 = userService.login(organizationMangerId, "123456");
        Response<String> login2 = userService.login(aliceId, "123456");
        Response<String> login3 = userService.login(bobId, "123456");
        organizationManagerToken = login1.getData();
        aliceToken = login2.getData();
        bobToken = login3.getData();

        Response<Integer> createOrganization = organizationService.createOrganization(organizationManagerToken,
                "Organization",
                "Description",
                "052-0520520",
                "organization@manager.com",
                organizationMangerId);
        organizationId = createOrganization.getData();
        Response<Integer> createVolunteering = organizationService.createVolunteering(organizationManagerToken,
                organizationId, "Volunteering", "Description", organizationMangerId);
        volunteeringId = createVolunteering.getData();
    }

    private void joinAliceToVolunteering(){
        volunteeringService.requestToJoinVolunteering(aliceToken, aliceId,volunteeringId,"Plz");
        volunteeringService.acceptUserJoinRequest(organizationManagerToken,organizationMangerId,volunteeringId,aliceId,0);
    }

    private int createAliceVolunteerPost(){
        Mockito.when(gemini.sendQuery(Mockito.anyString())).thenReturn("");
        Response<Integer> createVolunteerPost = postService.createVolunteerPost(aliceToken,aliceId,"Title","Description");
        if(createVolunteerPost.getError()){
            System.out.println(createVolunteerPost.getErrorString());
        }
        return createVolunteerPost.getData();
    }

    //4.16
    @Test
    public void whenSendMessageToVolunteering_givenManager_volunteerReceives(){
        Mockito.when(chatSocketSender.userOnChat(Mockito.anyString(),Mockito.anyString(),Mockito.any())).thenReturn(false);
        joinAliceToVolunteering();

        Response<Integer> sendVolunteeringMessage = chatService.sendVolunteeringMessage(organizationManagerToken,organizationMangerId,"Hello",volunteeringId);
        Assertions.assertFalse(sendVolunteeringMessage.getError());

        Response<List<MessageDTO>> getVolunteeringChatMessages1 = chatService.getVolunteeringChatMessages(aliceToken, aliceId,volunteeringId);
        Assertions.assertFalse(getVolunteeringChatMessages1.getError());
        List<MessageDTO> messages = getVolunteeringChatMessages1.getData();
        Assertions.assertEquals(1, messages.size());
        MessageDTO message = messages.get(0);
        Assertions.assertEquals(organizationMangerId, message.getSender());
        Assertions.assertEquals("Hello", message.getContent());

        Response<List<Notification>> getUserNotifications1 = userService.getUserNotifications(aliceToken,aliceId);
        Assertions.assertFalse(getUserNotifications1.getError());
        List<Notification> notifications1 = getUserNotifications1.getData();
        Notification notification = notifications1.get(0);
        Assertions.assertEquals("New message in chat of volunteering " + volunteeringId, notification.getMessage());

        Response<List<MessageDTO>> getVolunteeringChatMessages2 = chatService.getVolunteeringChatMessages(bobToken, bobId,volunteeringId);
        Assertions.assertTrue(getVolunteeringChatMessages2.getError());
        Assertions.assertEquals("User " + bobId + " has no permission to view " + " volunteering with id " + volunteeringId, getVolunteeringChatMessages2.getErrorString());

        Response<List<Notification>> getUserNotifications2 = userService.getUserNotifications(bobToken,bobId);
        Assertions.assertFalse(getUserNotifications2.getError());
        List<Notification> notifications = getUserNotifications2.getData();
        Assertions.assertEquals(0, notifications.size());
    }

    //2.7
    @Test
    public void whenSendMessageToVolunteering_givenVolunteer_managerReceives(){
        Mockito.when(chatSocketSender.userOnChat(Mockito.anyString(),Mockito.anyString(),Mockito.any())).thenReturn(false);
        joinAliceToVolunteering();

        Response<Integer> sendVolunteeringMessage = chatService.sendVolunteeringMessage(aliceToken,aliceId,"Hello",volunteeringId);
        Assertions.assertFalse(sendVolunteeringMessage.getError());

        Response<List<MessageDTO>> getVolunteeringChatMessages1 = chatService.getVolunteeringChatMessages(organizationManagerToken, organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringChatMessages1.getError());
        List<MessageDTO> messages = getVolunteeringChatMessages1.getData();
        Assertions.assertEquals(1, messages.size());
        MessageDTO message = messages.get(0);
        Assertions.assertEquals(aliceId, message.getSender());
        Assertions.assertEquals("Hello", message.getContent());

        Response<List<Notification>> getUserNotifications1 = userService.getUserNotifications(organizationManagerToken,organizationMangerId);
        Assertions.assertFalse(getUserNotifications1.getError());
        List<Notification> notifications1 = getUserNotifications1.getData();
        Notification notification = notifications1.get(0);
        Assertions.assertEquals("New message in chat of volunteering " + volunteeringId, notification.getMessage());

        Response<List<MessageDTO>> getVolunteeringChatMessages2 = chatService.getVolunteeringChatMessages(bobToken, bobId,volunteeringId);
        Assertions.assertTrue(getVolunteeringChatMessages2.getError());
        Assertions.assertEquals("User " + bobId + " has no permission to view " + " volunteering with id " + volunteeringId, getVolunteeringChatMessages2.getErrorString());

        Response<List<Notification>> getUserNotifications2 = userService.getUserNotifications(bobToken,bobId);
        Assertions.assertFalse(getUserNotifications2.getError());
        List<Notification> notifications = getUserNotifications2.getData();
        Assertions.assertEquals(0, notifications.size());
    }

    @Test
    public void whenSendMessageToVolunteering_givenOtherUser_thenError(){
        Mockito.when(chatSocketSender.userOnChat(Mockito.anyString(),Mockito.anyString(),Mockito.any())).thenReturn(false);
        joinAliceToVolunteering();
        Response<Integer> sendVolunteeringMessage = chatService.sendVolunteeringMessage(bobToken,bobId,"Hello",volunteeringId);
        Assertions.assertTrue(sendVolunteeringMessage.getError());
        Assertions.assertEquals("User " + bobId + " has no permission to view " + " volunteering with id " + volunteeringId, sendVolunteeringMessage.getErrorString());

        Response<List<MessageDTO>> getVolunteeringChatMessages1 = chatService.getVolunteeringChatMessages(organizationManagerToken, organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringChatMessages1.getError());
        List<MessageDTO> messages1 = getVolunteeringChatMessages1.getData();
        Assertions.assertEquals(0, messages1.size());
        Response<List<MessageDTO>> getVolunteeringChatMessages2 = chatService.getVolunteeringChatMessages(aliceToken, aliceId,volunteeringId);
        Assertions.assertFalse(getVolunteeringChatMessages2.getError());
        List<MessageDTO> messages2 = getVolunteeringChatMessages2.getData();
        Assertions.assertEquals(0, messages2.size());
    }

    //4.17,4.18
    @Test
    public void whenSendMessageToPost_givenAllValid_sendMessages(){
        Mockito.when(chatSocketSender.userOnChat(Mockito.anyString(),Mockito.anyString(),Mockito.any())).thenReturn(false);
        int postId = createAliceVolunteerPost();

        Response<Integer> sendPostMessage1 = chatService.sendPostMessage(bobToken,bobId,"Hello",postId,bobId);
        Assertions.assertFalse(sendPostMessage1.getError());

        Response<List<MessageDTO>> getPostChatMessages1 = chatService.getPostChatMessages(aliceToken, aliceId,postId,bobId);
        Assertions.assertFalse(getPostChatMessages1.getError());
        List<MessageDTO> messages1 = getPostChatMessages1.getData();
        Assertions.assertEquals(1, messages1.size());
        MessageDTO message1 = messages1.get(0);
        Assertions.assertEquals(bobId, message1.getSender());
        Assertions.assertEquals("Hello", message1.getContent());

        Response<List<Notification>> getUserNotifications1 = userService.getUserNotifications(aliceToken,aliceId);
        Assertions.assertFalse(getUserNotifications1.getError());
        List<Notification> notifications1 = getUserNotifications1.getData();
        Notification notification1 = notifications1.get(0);
        Assertions.assertEquals("New message in chat of your volunteer post " + postId, notification1.getMessage());

        Response<Integer> sendPostMessage2 = chatService.sendPostMessage(aliceToken,aliceId,"Hi!",postId,bobId);
        Assertions.assertFalse(sendPostMessage2.getError());

        Response<List<MessageDTO>> getPostChatMessages2 = chatService.getPostChatMessages(bobToken, bobId,postId,bobId);
        Assertions.assertFalse(getPostChatMessages2.getError());
        List<MessageDTO> messages2 = getPostChatMessages2.getData();
        Assertions.assertEquals(2, messages2.size());
        MessageDTO message2 = messages2.get(1);
        Assertions.assertEquals(aliceId, message2.getSender());
        Assertions.assertEquals("Hi!", message2.getContent());

        Response<List<Notification>> getUserNotifications2 = userService.getUserNotifications(bobToken,bobId);
        Assertions.assertFalse(getUserNotifications2.getError());
        List<Notification> notifications2 = getUserNotifications2.getData();
        Notification notification2 = notifications2.get(0);
        Assertions.assertEquals("New message in chat of volunteer post " + postId, notification2.getMessage());
    }

    @Test
    public void whenSendMessageToPost_givenPostDoesntExist_thenError(){
        Mockito.when(chatSocketSender.userOnChat(Mockito.anyString(),Mockito.anyString(),Mockito.any())).thenReturn(false);
        int postId = 0;

        Response<Integer> sendPostMessage1 = chatService.sendPostMessage(bobToken,bobId,"Hello",postId,bobId);
        Assertions.assertTrue(sendPostMessage1.getError());
        Assertions.assertEquals(PostErrors.makePostIdDoesNotExistError(postId), sendPostMessage1.getErrorString());

        Response<List<MessageDTO>> getPostChatMessages1 = chatService.getPostChatMessages(aliceToken, aliceId,postId,bobId);
        Assertions.assertTrue(getPostChatMessages1.getError());
        Assertions.assertEquals(PostErrors.makePostIdDoesNotExistError(postId), getPostChatMessages1.getErrorString());


        Response<List<MessageDTO>> getPostChatMessages2 = chatService.getPostChatMessages(bobToken, bobId,postId,bobId);
        Assertions.assertTrue(getPostChatMessages2.getError());
        Assertions.assertEquals(PostErrors.makePostIdDoesNotExistError(postId), getPostChatMessages2.getErrorString());
    }

    @Test
    public void whenSendMessageToPost_givenChatClosed_thenError(){
        Mockito.when(chatSocketSender.userOnChat(Mockito.anyString(),Mockito.anyString(),Mockito.any())).thenReturn(false);
        int postId = createAliceVolunteerPost();

        Response<Integer> sendPostMessage1 = chatService.sendPostMessage(aliceToken,aliceId,"Hello",postId,bobId);
        Assertions.assertTrue(sendPostMessage1.getError());
        Assertions.assertEquals("Chat is closed", sendPostMessage1.getErrorString());

        Response<List<MessageDTO>> getPostChatMessages1 = chatService.getPostChatMessages(aliceToken, aliceId,postId,bobId);
        Assertions.assertFalse(getPostChatMessages1.getError());
        List<MessageDTO> messages1 = getPostChatMessages1.getData();
        Assertions.assertEquals(0, messages1.size());

        Response<List<MessageDTO>> getPostChatMessages2 = chatService.getPostChatMessages(bobToken, bobId,postId,bobId);
        Assertions.assertFalse(getPostChatMessages2.getError());
        List<MessageDTO> messages2 = getPostChatMessages2.getData();
        Assertions.assertEquals(0, messages2.size());
    }

    @Test
    public void whenSendMessageToPost_givenInvalidContent_thenError(){
        Mockito.when(chatSocketSender.userOnChat(Mockito.anyString(),Mockito.anyString(),Mockito.any())).thenReturn(false);
        int postId = createAliceVolunteerPost();

        Response<Integer> sendPostMessage1 = chatService.sendPostMessage(bobToken,bobId,"",postId,bobId);
        Assertions.assertTrue(sendPostMessage1.getError());
        Assertions.assertEquals("Message cannot be empty", sendPostMessage1.getErrorString());

        Response<List<MessageDTO>> getPostChatMessages1 = chatService.getPostChatMessages(aliceToken, aliceId,postId,bobId);
        Assertions.assertFalse(getPostChatMessages1.getError());
        List<MessageDTO> messages1 = getPostChatMessages1.getData();
        Assertions.assertEquals(0, messages1.size());

        Response<List<MessageDTO>> getPostChatMessages2 = chatService.getPostChatMessages(bobToken, bobId,postId,bobId);
        Assertions.assertFalse(getPostChatMessages2.getError());
        List<MessageDTO> messages2 = getPostChatMessages2.getData();
        Assertions.assertEquals(0, messages2.size());
    }
}
