package com.dogood.dogoodbackend.domain.chat;

import com.dogood.dogoodbackend.jparepos.MessageJPA;

import java.util.Date;
import java.util.List;

public class DatabaseMessageRepository implements MessageRepository{
    private MessageJPA jpa;

    public DatabaseMessageRepository(MessageJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public Message createMessage(String content, String senderId, String receiverId, ReceiverType type) {
        Message m = new Message(content, senderId, receiverId, type, new Date());
        jpa.save(m);
        return m;
    }

    @Override
    public void deleteMessage(int messageId) {
        getMessage(messageId);
        jpa.deleteById(messageId);
    }

    @Override
    public void editMessage(int messageId, String newContent) {
        Message m = getMessage(messageId);
        m.setContent(newContent);
        jpa.save(m);
    }

    @Override
    public List<Message> getVolunteeringChatMessages(int volunteeringId) {
        return jpa.findByReceiverIdAndAndReceiverType(""+volunteeringId,ReceiverType.VOLUNTEERING);
    }

    @Override
    public List<Message> getPrivateChatMessages(String sender, String receiver) {
        return jpa.findByReceiverIdAndAndReceiverTypeAndSenderId(receiver,ReceiverType.USER, sender);
    }

    @Override
    public Message getMessage(int messageId) {
        Message m = jpa.findById(messageId).orElse(null);
        if (m == null){
            throw new IllegalArgumentException("Message with ID " + messageId + " not found");
        }
        return m;
    }
}
