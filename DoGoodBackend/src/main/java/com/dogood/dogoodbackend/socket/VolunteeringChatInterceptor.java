package com.dogood.dogoodbackend.socket;


import com.dogood.dogoodbackend.service.FacadeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class VolunteeringChatInterceptor implements ChannelInterceptor {
    @Autowired
    private FacadeManager facadeManager;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();
        if(command == StompCommand.CONNECT) {
            List<String> authList = accessor.getNativeHeader("Authorization");
            if(authList == null || authList.isEmpty()){
                throw new IllegalArgumentException("Missing 'Authorization' header");
            }
            String token = authList.get(0);
            Authentication auth = new UsernamePasswordAuthenticationToken(facadeManager.getAuthFacade().getNameFromToken(token), null, null);
            accessor.setUser(auth);
        } else if (command == StompCommand.SUBSCRIBE) {
            if(accessor.getDestination() != null && accessor.getDestination().startsWith("/topic/volchat/")){
                int volunteeringId = Integer.parseInt(accessor.getDestination().substring("/topic/volchat/".length()));
                facadeManager.getVolunteeringFacade().checkViewingPermissions(accessor.getUser().getName(),volunteeringId);
            }
        }
        return message;
    }
}
