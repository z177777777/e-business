package com.ebusiness.controller;

import com.ebusiness.entity.ChatMessage;
import com.ebusiness.repository.ChatMessageRepository;
import java.util.Map;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWsController {

  private final SimpMessagingTemplate messagingTemplate;
  private final ChatMessageRepository chatMessageRepository;

  public ChatWsController(SimpMessagingTemplate messagingTemplate,
      ChatMessageRepository chatMessageRepository) {
    this.messagingTemplate = messagingTemplate;
    this.chatMessageRepository = chatMessageRepository;
  }

  @MessageMapping("/chat.send")
  public void sendMessage(@Payload Map<String, Object> payload) {
    Long senderId = Long.valueOf(payload.get("senderId").toString());
    Long receiverId = Long.valueOf(payload.get("receiverId").toString());
    String content = payload.get("content") != null ? payload.get("content").toString() : null;
    String messageType = payload.get("messageType") != null ? payload.get("messageType").toString() : "TEXT";
    Long orderId = null;
    if (payload.get("orderId") != null) {
      orderId = Long.valueOf(payload.get("orderId").toString());
    }

    ChatMessage msg = new ChatMessage();
    msg.setSenderId(senderId);
    msg.setReceiverId(receiverId);
    msg.setContent(content);
    msg.setMessageType(messageType);
    msg.setOrderId(orderId);
    chatMessageRepository.save(msg);

    Map<String, Object> response = new java.util.HashMap<>();
    response.put("id", msg.getId());
    response.put("senderId", msg.getSenderId());
    response.put("receiverId", msg.getReceiverId());
    response.put("content", msg.getContent());
    response.put("messageType", msg.getMessageType());
    response.put("orderId", msg.getOrderId());
    response.put("createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null);

    messagingTemplate.convertAndSendToUser(
        String.valueOf(receiverId), "/queue/chat", response);

    messagingTemplate.convertAndSendToUser(
        String.valueOf(senderId), "/queue/chat", response);
  }

  @MessageMapping("/chat.markRead")
  public void markRead(@Payload Map<String, Object> payload) {
    // Placeholder for future read-receipt functionality
  }
}
