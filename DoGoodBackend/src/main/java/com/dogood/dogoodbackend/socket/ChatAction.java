package com.dogood.dogoodbackend.socket;

import com.dogood.dogoodbackend.domain.chat.MessageDTO;

public class ChatAction {
    private ChatActionType type;
    private MessageDTO payload;

    public ChatAction(ChatActionType type, MessageDTO payload) {
        this.type = type;
        this.payload = payload;
    }

    public ChatAction() {
    }

    public ChatActionType getType() {
        return type;
    }

    public MessageDTO getPayload() {
        return payload;
    }
}
