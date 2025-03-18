package com.dogood.dogoodbackend.domain.chat;

import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.jparepos.MessageJPA;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class ChatFacadeTests {
    private ChatFacade chatFacade;
    private VolunteeringFacade volunteeringFacade;

    @Autowired
    private MessageJPA jpa;

    @BeforeEach
    public void setUp() {
        jpa.deleteAll();
        this.volunteeringFacade = Mockito.mock(VolunteeringFacade.class);
        this.chatFacade = new ChatFacade(volunteeringFacade, new DatabaseMessageRepository(jpa));
    }

    @Test
    public void givenHasPermissions_whenSendVolunteeringMessage_thenSendMessage(){
        Mockito.doNothing().when(volunteeringFacade).checkViewingPermissions(Mockito.anyString(), Mockito.anyInt());
        int id = chatFacade.sendVolunteeringMessage("Eyal","Hello!", 0);
        Message m = jpa.findById(id).orElse(null);
        Assertions.assertNotEquals(null, m);
        Assertions.assertEquals("Hello!", m.getContent());
        Assertions.assertEquals("0", m.getReceiverId());
        Assertions.assertEquals(ReceiverType.VOLUNTEERING, m.getReceiverType());
    }

    @Test
    public void givenNoPermissions_whenSendVolunteeringMessage_thenThrowException(){
        Mockito.doThrow(IllegalArgumentException.class).when(volunteeringFacade).checkViewingPermissions("Eyal", 0);
        Assertions.assertThrows(IllegalArgumentException.class, () -> chatFacade.sendVolunteeringMessage("Eyal","Hello!", 0));
    }

    @Test
    public void givenMessageSender_whenDeleteMessage_thenDeleteMessage(){
        int id = chatFacade.sendPrivateMessage("Eyal","Hello!", "Dana");
        chatFacade.deleteMessage("Eyal", id);
        Message m = jpa.findById(id).orElse(null);
        Assertions.assertNull(m);
    }

    @Test
    public void givenNotMessageSender_whenDeleteMessage_thenThrowException(){
        int id = chatFacade.sendPrivateMessage("Eyal","Hello!", "Dana");
        Assertions.assertThrows(IllegalCallerException.class, ()->chatFacade.deleteMessage("Dana", id));
        Message m = jpa.findById(id).orElse(null);
        Assertions.assertNotNull(m);
    }

    @Test
    public void givenMessageSender_whenEditMessage_thenEditMessage(){
        int id = chatFacade.sendPrivateMessage("Eyal","Hello!", "Dana");
        chatFacade.editMessage("Eyal", id, "Hola");
        Message m = jpa.findById(id).orElse(null);
        Assertions.assertNotNull(m);
        Assertions.assertEquals("Hola", m.getContent());
    }

    @Test
    public void givenNotMessageSender_whenEditMessage_thenThrowException(){
        int id = chatFacade.sendPrivateMessage("Eyal","Hello!", "Dana");
        Assertions.assertThrows(IllegalCallerException.class, ()->chatFacade.editMessage("Dana", id, "Hola"));
        Message m = jpa.findById(id).orElse(null);
        Assertions.assertNotNull(m);
        Assertions.assertEquals("Hello!", m.getContent());
    }

    @Test
    public void givenMessageSender_whenGetMessages_thenIsSenderTrue(){
        chatFacade.sendPrivateMessage("Eyal","Hello!", "Dana");
        chatFacade.sendPrivateMessage("Dana","Hello World!", "Eyal");
        List<MessageDTO> messages = chatFacade.getPrivateChatMessages("Eyal", "Dana");
        Assertions.assertEquals(2, messages.size());
        MessageDTO m = messages.get(0);
        Assertions.assertEquals("Hello!", m.getContent());
        Assertions.assertTrue(m.isUserIsSender());
        MessageDTO m2 = messages.get(1);
        Assertions.assertFalse(m2.isUserIsSender());
    }

    @Test
    public void givenMessageSender_whenGetVolunteeringMessages_thenIsSenderTrue(){
        Mockito.doNothing().when(volunteeringFacade).checkViewingPermissions(Mockito.anyString(), Mockito.anyInt());
        chatFacade.sendVolunteeringMessage("Eyal","Hello!", 0);
        chatFacade.sendVolunteeringMessage("Dana","Hello World!", 0);
        List<MessageDTO> messages = chatFacade.getVolunteeringChatMessages("Eyal", 0);
        Assertions.assertEquals(2, messages.size());
        MessageDTO m = messages.get(0);
        Assertions.assertEquals("Hello!", m.getContent());
        Assertions.assertTrue(m.isUserIsSender());
        MessageDTO m2 = messages.get(1);
        Assertions.assertFalse(m2.isUserIsSender());
    }
}
