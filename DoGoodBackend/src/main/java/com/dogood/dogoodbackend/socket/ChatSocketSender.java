package com.dogood.dogoodbackend.socket;

import com.dogood.dogoodbackend.domain.chat.MessageDTO;
import com.dogood.dogoodbackend.domain.chat.ReceiverType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatSocketSender {
    @Autowired
    SimpMessagingTemplate template;

    public void sendMessageVolunteering(MessageDTO message, int volunteeringId) {
        template.convertAndSend("/topic/volchat/"+volunteeringId, new ChatAction(ChatActionType.ADD,message));
    }

    public void deleteMessage(int messageId, String receiverId, ReceiverType type) {
        ChatAction action = new ChatAction(ChatActionType.DELETE, new MessageDTO(messageId, "", "", null, false));
        sendAction(action, receiverId, type);
    }

    public void editMessage(int messageId, String receiverId, String newContent, ReceiverType type) {
        ChatAction action = new ChatAction(ChatActionType.EDIT, new MessageDTO(messageId, "", newContent, null, false));
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

}
