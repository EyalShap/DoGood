package com.dogood.dogoodbackend.domain.chat;

import java.util.List;

public interface MessageRepository {
    public Message createMessage(String content, String senderId, String receiverId, ReceiverType type);
    public void deleteMessage(int messageId);
    public void editMessage(int messageId, String newContent);
    public List<Message> getVolunteeringChatMessages(int volunteeringId);
    public List<Message> getPrivateChatMessages(String sender, String receiver);
    public List<Message> getPostChatMessages(String with, int postId);
    public Message getMessage(int messageId);
    public List<String> getSendersToPost(int postId);
    public void closePostChat(int postId, String with);
}
