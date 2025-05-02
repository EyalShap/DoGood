package com.dogood.dogoodbackend.domain.users.notificiations;

import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationSystem {
    private NotificationRepository repository;
    private NotificationSocketSender sender;
    private FirebaseMessaging firebaseMessaging;
    private UsersFacade usersFacade;

    public NotificationSystem(NotificationRepository repository) {
        this.repository = repository;
    }

    public void setSender(NotificationSocketSender sender) {
        this.sender = sender;
    }

    public void notifyUser(String username, String message, String navigationURL) {
        Notification notification = repository.createNotification(username, message, navigationURL);
        sender.sendNotification(username, notification);
        Set<String> expiredTokens = new HashSet<>();
        List<String> fcmTokens = new ArrayList<>(usersFacade.getFcmTokens(username));
        if(firebaseMessaging != null && fcmTokens.size() > 0) {
                try {
                    BatchResponse response = firebaseMessaging
                            .sendEach(fcmTokens.stream().map(fcmToken -> com.google.firebase.messaging.Message.builder().setNotification(com.google.firebase.messaging
                                    .Notification.builder()
                                    .setTitle("New Notification from DoGood")
                                    .setBody(message).build()).setToken(fcmToken).build()).toList());
                    if(response.getFailureCount() > 0){
                        List<SendResponse> responses = response.getResponses();
                        for(int i = 0; i < responses.size(); i++){
                            if(!responses.get(i).isSuccessful()){
                                expiredTokens.add(fcmTokens.get(i));
                            }
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
        }
        usersFacade.expireFcmTokens(username, expiredTokens);
    }

    public void setFirebaseMessaging(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public List<Notification> getUserNotifications(String username) {
        return repository.getUserNotifications(username);
    }

    public List<Notification> readNewUserNotifications(String username) {
        return repository.readNewUserNotifications(username);
    }

    public Integer getNewUserNotificationsAmount(String username) {
        return repository.getUserNotifications(username).stream().filter(x-> !x.getIsRead()).toList().size();
    }

    public void setUsersFacade(UsersFacade usersFacade) {
        this.usersFacade = usersFacade;
    }
}
