package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.domain.chat.MessageDTO;
import com.dogood.dogoodbackend.service.ChatService;
import com.dogood.dogoodbackend.service.Response;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.dogood.dogoodbackend.utils.GetToken.getToken;

@RestController
@CrossOrigin
@RequestMapping("/api/chat")
public class ChatAPI {

    @Autowired
    ChatService chatService;

    @PostMapping("/sendVolunteeringMessage")
    public Response<Integer> sendVolunteeringMessage(@RequestBody JsonNode body, HttpServletRequest request){
        String token = getToken(request);
        return chatService.sendVolunteeringMessage(token, body.get("username").asText(), body.get("content").asText(), body.get("volunteeringId").asInt());
    }

    @PostMapping("/sendPostMessage")
    public Response<Integer> sendPostMessage(@RequestBody JsonNode body, HttpServletRequest request){
        String token = getToken(request);
        return chatService.sendPostMessage(token, body.get("username").asText(), body.get("content").asText(), body.get("postId").asInt(), body.get("with").asText());
    }

    @PostMapping("/sendPrivateMessage")
    public Response<Integer> sendPrivateMessage(@RequestBody JsonNode body, HttpServletRequest request){
        String token = getToken(request);
        return chatService.sendPrivateMessage(token, body.get("username").asText(), body.get("content").asText(), body.get("receiverId").asText());
    }

    @DeleteMapping("/deleteMessage")
    public Response<String> deleteMessage(@RequestParam String username, @RequestParam int messageId, HttpServletRequest request){
        String token = getToken(request);
        return chatService.deleteMessage(token, username, messageId);
    }

    @PatchMapping("/editMessage")
    public Response<String> editMessage(@RequestBody JsonNode body, HttpServletRequest request){
        String token = getToken(request);
        return chatService.editMessage(token, body.get("username").asText(), body.get("messageId").asInt(), body.get("newContent").asText());
    }

    @GetMapping("/getPrivateChatMessages")
    public Response<List<MessageDTO>> getPrivateChatMessages(@RequestParam String username, @RequestParam String receiverId, HttpServletRequest request){
        String token = getToken(request);
        return chatService.getPrivateChatMessages(token, username, receiverId);
    }

    @GetMapping("/getVolunteeringChatMessages")
    public Response<List<MessageDTO>> getVolunteeringChatMessages(@RequestParam String username, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return chatService.getVolunteeringChatMessages(token, username, volunteeringId);
    }

    @GetMapping("/getPostChatMessages")
    public Response<List<MessageDTO>> getPostChatMessages(@RequestParam String username, @RequestParam int postId, @RequestParam String with, HttpServletRequest request){
        String token = getToken(request);
        return chatService.getPostChatMessages(token, username, postId, with);
    }
}
