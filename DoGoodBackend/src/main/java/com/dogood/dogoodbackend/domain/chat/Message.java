package com.dogood.dogoodbackend.domain.chat;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 100000)
    private String content;
    private String senderId;
    private String receiverId;
    private ReceiverType receiverType;
    private Date timeSent;
    private boolean edited;
    private Date timeEdited;

    public Message(String content, String senderId, String receiverId, ReceiverType receiverType, Date timeSent) {
        this.content = content;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.receiverType = receiverType;
        this.timeSent = timeSent;
        this.edited = false;
        this.timeEdited = null;
    }

    public Message() {
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public ReceiverType getReceiverType() {
        return receiverType;
    }

    public Date getTimeSent() {
        return timeSent;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public Date getTimeEdited() {
        return timeEdited;
    }

    public void setTimeEdited(Date timeEdited) {
        this.timeEdited = timeEdited;
    }

    public void setContent(String content) {
        this.content = content;
        this.edited = true;
        this.timeEdited = new Date();
    }

    public MessageDTO getDtoForUser(String username){
        return new MessageDTO(id,senderId,content,timeSent,senderId.equals(username),edited,timeEdited);
    }
}
