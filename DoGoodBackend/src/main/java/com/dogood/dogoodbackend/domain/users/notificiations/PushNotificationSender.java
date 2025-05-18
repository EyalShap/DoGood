package com.dogood.dogoodbackend.domain.users.notificiations;

import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.SendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PushNotificationSender {
    @Autowired
    private FirebaseMessaging firebaseMessaging;
    private UsersFacade usersFacade;

    public void setUsersFacade(UsersFacade usersFacade) {
        this.usersFacade = usersFacade;
    }

    @Async
    public void sendPush(String username, String message, String navigationURL){
        Set<String> expiredTokens = new HashSet<>();
        List<String> fcmTokens = new ArrayList<>(usersFacade.getFcmTokens(username));
        if(firebaseMessaging != null && fcmTokens.size() > 0) {
            try {
                BatchResponse response = firebaseMessaging
                        .sendEach(fcmTokens.stream().map(fcmToken -> com.google.firebase.messaging.Message.builder()
                                .putData("body",message)
                                .putData("title","New Notification from DoGood")
                                .putData("click_action",navigationURL)
                                .setToken(fcmToken).build()).toList());
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
}
