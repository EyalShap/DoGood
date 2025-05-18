package com.dogood.dogoodbackend.domain.chat;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;

public class MessageDTO implements Comparable<MessageDTO> {
    private int id;

    private String sender;
    private String content;
    private Date timeSent;
    private boolean userIsSender;
    private boolean edited;
    private Date timeEdited;


    public MessageDTO(int id, String sender, String content, Date timeSent, boolean userIsSender, boolean edited, Date timeEdited) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.timeSent = timeSent;
        this.userIsSender = userIsSender;
        this.edited = edited;
        this.timeEdited = timeEdited;
    }

    public MessageDTO() {
    }

    public int getId() {
        return id;
    }

    public boolean isUserIsSender() {
        return userIsSender;
    }

    public String getContent() {
        return content;
    }

    public Date getTimeSent() {
        return timeSent;
    }

    public String getSender() {
        return sender;
    }

    public boolean isEdited() {
        return edited;
    }

    public Date getTimeEdited() {
        return timeEdited;
    }

    @Override
    public int compareTo(MessageDTO o) {
        return this.timeSent.compareTo(o.timeSent);
    }
}
