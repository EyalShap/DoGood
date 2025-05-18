package com.dogood.dogoodbackend.socket;

import com.dogood.dogoodbackend.domain.chat.MessageDTO;
import com.dogood.dogoodbackend.domain.chat.ReceiverType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ChatSocketSender {
    @Autowired
    SimpMessagingTemplate template;

    @Autowired
    SimpUserRegistry registry;

    public void sendMessageVolunteering(MessageDTO message, int volunteeringId) {
        template.convertAndSend("/topic/volchat/"+volunteeringId, new ChatAction(ChatActionType.ADD,message));
    }

    public void deleteMessage(int messageId, String receiverId, ReceiverType type) {
        ChatAction action = new ChatAction(ChatActionType.DELETE, new MessageDTO(messageId, "", "", null, false,false,null));
        sendAction(action, receiverId, type);
    }

    public void editMessage(int messageId, String receiverId, String newContent, ReceiverType type) {
        ChatAction action = new ChatAction(ChatActionType.EDIT, new MessageDTO(messageId, "", newContent, null, false,true,new Date()));
        sendAction(action, receiverId, type);
    }

    private void sendAction(ChatAction action, String receiverId, ReceiverType type) {
        String loc = "";
        if(type == ReceiverType.VOLUNTEERING) {
            loc = "/topic/volchat/" + receiverId;
        }else if(type == ReceiverType.POST){
            String[] splits = receiverId.split("@");
            loc = "/topic/postchat/" + splits[1] + "/" + splits[0];
        }
        template.convertAndSend(loc, action);
    }

    public void sendMessagePost(MessageDTO message, String with, int postId) {
        template.convertAndSend("/topic/postchat/"+postId + "/"+with, new ChatAction(ChatActionType.ADD,message));
    }

    public void closeChat(String with, int postId) {
        template.convertAndSend("/topic/postchat/" + postId + "/" + with, new ChatAction(ChatActionType.CLOSE, null));
    }

    public boolean userOnChat(String username, String receiverId, ReceiverType type) {
        String loc = "";
        if(type == ReceiverType.VOLUNTEERING) {
            loc = "/topic/volchat/" + receiverId;
        }else if(type == ReceiverType.POST){
            String[] splits = receiverId.split("@");
            loc = "/topic/postchat/" + splits[1] + "/" + splits[0];
        }
        SimpUser user = registry.getUser(username);
        if(user == null) {
            return false;
        }
        for(SimpSession session : user.getSessions()){
            for(SimpSubscription sub : session.getSubscriptions()){
                if(sub.getDestination().equals(loc)){
                    return true;
                }
            }
        }
        return false;
    }
}
