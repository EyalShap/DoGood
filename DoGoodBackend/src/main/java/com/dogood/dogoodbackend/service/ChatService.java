package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.chat.Message;
import com.dogood.dogoodbackend.domain.chat.MessageDTO;
import com.dogood.dogoodbackend.domain.chat.ReceiverType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    private FacadeManager facadeManager;

    public ChatService(FacadeManager facadeManager) {
        this.facadeManager = facadeManager;
    }

    private void checkToken(String token, String username) {
        if (!facadeManager.getAuthFacade().getNameFromToken(token).equals(username)) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public Response<Integer> sendVolunteeringMessage(String token, String username, String content, int volunteeringId){
        try{
            checkToken(token, username);
            int id = facadeManager.getChatFacade().sendVolunteeringMessage(username, content, volunteeringId);
            return Response.createResponse(id);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> sendPrivateMessage(String token, String username, String content, String receiverId){
        try{
            checkToken(token, username);
            int id = facadeManager.getChatFacade().sendPrivateMessage(username, content, receiverId);
            return Response.createResponse(id);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> deleteMessage(String token, String username, int messageId){
        try{
            checkToken(token, username);
            facadeManager.getChatFacade().deleteMessage(username, messageId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> editMessage(String token, String username, int messageId, String newContent){
        try{
            checkToken(token, username);
            facadeManager.getChatFacade().editMessage(username, messageId, newContent);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<MessageDTO>> getVolunteeringChatMessages(String token, String username, int volunteeringId){
        try{
            checkToken(token, username);
            return Response.createResponse(facadeManager.getChatFacade().getVolunteeringChatMessages(username,volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<MessageDTO>> getPrivateChatMessages(String token, String username, String receiverId){
        try{
            checkToken(token, username);
            return Response.createResponse(facadeManager.getChatFacade().getPrivateChatMessages(username,receiverId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }
}
