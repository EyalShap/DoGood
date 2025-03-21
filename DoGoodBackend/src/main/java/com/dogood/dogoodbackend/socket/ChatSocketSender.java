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
        if(type == ReceiverType.VOLUNTEERING) {
            template.convertAndSend("/topic/volchat/" + receiverId, new ChatAction(ChatActionType.DELETE, new MessageDTO(messageId, "", "", null, false)));
        }
    }

    public void editMessage(int messageId, String receiverId, String newContent, ReceiverType type) {
        if(type == ReceiverType.VOLUNTEERING) {
            template.convertAndSend("/topic/volchat/" + receiverId, new ChatAction(ChatActionType.EDIT, new MessageDTO(messageId, "", newContent, null, false)));
        }
    }
}
