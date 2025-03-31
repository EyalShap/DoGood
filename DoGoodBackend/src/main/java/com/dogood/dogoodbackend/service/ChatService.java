package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.chat.Message;
import com.dogood.dogoodbackend.domain.chat.MessageDTO;
import com.dogood.dogoodbackend.domain.chat.ReceiverType;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    private FacadeManager facadeManager;

    @Autowired
    public ChatService(FacadeManager facadeManager, ChatSocketSender chatSocketSender) {
        this.facadeManager = facadeManager;
        this.facadeManager.getChatFacade().setChatSocketSender(chatSocketSender);
    }

    private void checkToken(String token, String username) {
        if (!facadeManager.getAuthFacade().getNameFromToken(token).equals(username)) {
            throw new IllegalArgumentException("Invalid token");
        }
        if (facadeManager.getUsersFacade().isBanned(username)) {
            throw new IllegalArgumentException("Banned user.");
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

    public Response<Integer> sendPostMessage(String token, String username, String content, int postId, String with){
        try{
            checkToken(token, username);
            int id = facadeManager.getChatFacade().sendPostMessage(username, content, postId, with);
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

    public Response<List<MessageDTO>> getPostChatMessages(String token, String username, int postId, String with) {
        try{
            checkToken(token, username);
            return Response.createResponse(facadeManager.getChatFacade().getPostChatMessages(username,postId,with));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }
}
