package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.common.BusinessException;
import com.ebusiness.common.CurrentUserUtil;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.entity.ChatMessage;
import com.ebusiness.entity.User;
import com.ebusiness.repository.ChatMessageRepository;
import com.ebusiness.repository.UserRepository;
import com.ebusiness.service.FileService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

  private final ChatMessageRepository chatMessageRepository;
  private final UserRepository userRepository;
  private final FileService fileService;

  public ChatController(ChatMessageRepository chatMessageRepository,
      UserRepository userRepository,
      FileService fileService) {
    this.chatMessageRepository = chatMessageRepository;
    this.userRepository = userRepository;
    this.fileService = fileService;
  }

  @GetMapping("/sessions")
  public ApiResponse<List<Map<String, Object>>> getSessions() {
    Long currentUserId = CurrentUserUtil.getCurrentUserId();
    User currentUser = userRepository.findById(currentUserId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    List<Map<String, Object>> sessions = new ArrayList<>();

    if ("CSR".equals(currentUser.getRole())) {
      List<Long> partnerIds = chatMessageRepository.findConversationPartners(currentUserId);
      for (Long partnerId : partnerIds) {
        User partner = userRepository.findById(partnerId).orElse(null);
        if (partner == null) continue;
        Map<String, Object> session = new HashMap<>();
        session.put("userId", partner.getId());
        session.put("nickname", partner.getNickname());
        session.put("email", partner.getEmail());
        session.put("avatarUrl", partner.getAvatarUrl());
        sessions.add(session);
      }
    } else {
      // 普通用户：查询所有CSR，按负载（活跃会话数）排序，最闲的自动推荐
      List<User> csrs = userRepository.findByRole("CSR");
      for (User csr : csrs) {
        int activeCount = chatMessageRepository.findConversationPartners(csr.getId()).size();
        Map<String, Object> session = new HashMap<>();
        session.put("userId", csr.getId());
        session.put("nickname", csr.getNickname());
        session.put("email", csr.getEmail());
        session.put("avatarUrl", csr.getAvatarUrl());
        session.put("activeSessions", activeCount);
        sessions.add(session);
      }
      sessions.sort(Comparator.comparingInt(s -> (int) s.getOrDefault("activeSessions", 0)));
      if (!sessions.isEmpty()) {
        sessions.get(0).put("recommended", true);
      }
    }

    return ApiResponse.success(sessions);
  }

  @GetMapping("/messages")
  public ApiResponse<List<Map<String, Object>>> getMessages(
      @RequestParam Long targetUserId,
      @RequestParam(required = false) String before,
      @RequestParam(defaultValue = "30") int size) {
    Long currentUserId = CurrentUserUtil.getCurrentUserId();

    Page<ChatMessage> page;
    if (before != null && !before.isEmpty()) {
      LocalDateTime beforeTime = LocalDateTime.parse(before);
      page = chatMessageRepository.findConversationPaged(currentUserId, targetUserId,
          PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    } else {
      page = chatMessageRepository.findConversationPaged(currentUserId, targetUserId,
          PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    List<Map<String, Object>> result = page.getContent().stream().map(m -> {
      Map<String, Object> msg = new HashMap<>();
      msg.put("id", m.getId());
      msg.put("senderId", m.getSenderId());
      msg.put("receiverId", m.getReceiverId());
      msg.put("content", m.getContent());
      msg.put("messageType", m.getMessageType());
      msg.put("orderId", m.getOrderId());
      msg.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
      return msg;
    }).collect(Collectors.toList());

    java.util.Collections.reverse(result);

    return ApiResponse.success(result);
  }

  @PostMapping("/upload")
  public ApiResponse<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
    String url = fileService.storeImage(file);
    Map<String, String> result = new HashMap<>();
    result.put("url", url);
    return ApiResponse.success(result);
  }
}
