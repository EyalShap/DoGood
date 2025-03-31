package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.chat.Message;
import com.dogood.dogoodbackend.domain.chat.ReceiverType;
import com.dogood.dogoodbackend.domain.volunteerings.Volunteering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageJPA extends JpaRepository<Message, Integer> {
    List<Message> findByReceiverIdAndAndReceiverType(String receiverId, ReceiverType receiverType);
    List<Message> findByReceiverIdAndAndReceiverTypeAndSenderId(String receiverId, ReceiverType receiverType, String senderId);
    List<Message> findByReceiverIdEndsWithAndReceiverType(String receiverId, ReceiverType receiverType);
}
