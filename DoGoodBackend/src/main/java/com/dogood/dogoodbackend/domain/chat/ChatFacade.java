package com.dogood.dogoodbackend.domain.chat;

import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.socket.ChatSocketSender;

import java.util.List;

public class ChatFacade {
    private MessageRepository repository;
    private VolunteeringFacade volunteeringFacade;
    private ChatSocketSender chatSocketSender;

    public ChatFacade(VolunteeringFacade volunteeringFacade, MessageRepository repository) {
        this.repository = repository;
        this.volunteeringFacade = volunteeringFacade;
    }

    public void setChatSocketSender(ChatSocketSender chatSocketSender) {
        this.chatSocketSender = chatSocketSender;
    }

    public int sendVolunteeringMessage(String username, String content, int volunteeringId){
        volunteeringFacade.checkViewingPermissions(username, volunteeringId);
        Message m = repository.createMessage(content,username,""+volunteeringId,ReceiverType.VOLUNTEERING);
        chatSocketSender.sendMessageVolunteering(m.getDtoForUser(""),volunteeringId);
        return m.getId();
    }

    public int sendPrivateMessage(String username, String content, String receiverId){
        if(username.equals(receiverId)){
            throw new IllegalArgumentException("You cannot send messages to yourself");
        }
        Message m = repository.createMessage(content,username,receiverId,ReceiverType.USER);
        // socket
        return m.getId();
    }

    public void deleteMessage(String username, int messageId){
        Message message = repository.getMessage(messageId);
        if(!message.getSenderId().equals(username)){
            throw new IllegalCallerException("Only message sender can delete a message");
        }
        String receiverId = message.getReceiverId();
        ReceiverType receiverType = message.getReceiverType();
        repository.deleteMessage(messageId);
        chatSocketSender.deleteMessage(messageId,receiverId,receiverType);
    }
    public void editMessage(String username, int messageId, String newContent){
        Message message = repository.getMessage(messageId);
        if(!message.getSenderId().equals(username)){
            throw new IllegalCallerException("Only message sender can edit a message");
        }
        String receiverId = message.getReceiverId();
        ReceiverType receiverType = message.getReceiverType();
        repository.editMessage(messageId, newContent);
        chatSocketSender.editMessage(messageId,receiverId,newContent,receiverType);
    }
    public List<MessageDTO> getVolunteeringChatMessages(String username, int volunteeringId){
        volunteeringFacade.checkViewingPermissions(username, volunteeringId);
        return repository.getVolunteeringChatMessages(volunteeringId).stream().map(message -> message.getDtoForUser(username)).sorted().toList();
    }
    public List<MessageDTO> getPrivateChatMessages(String username, String user2){
        List<Message> chat = repository.getPrivateChatMessages(username,user2);
        chat.addAll(repository.getPrivateChatMessages(user2, username));
        return chat.stream().map(message -> message.getDtoForUser(username)).sorted().toList();
    }
}
