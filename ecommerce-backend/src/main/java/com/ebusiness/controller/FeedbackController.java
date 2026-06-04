package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.common.CurrentUserUtil;
import com.ebusiness.entity.Feedback;
import com.ebusiness.entity.User;
import com.ebusiness.repository.FeedbackRepository;
import com.ebusiness.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

  private final FeedbackRepository feedbackRepository;
  private final UserRepository userRepository;

  public FeedbackController(FeedbackRepository feedbackRepository, UserRepository userRepository) {
    this.feedbackRepository = feedbackRepository;
    this.userRepository = userRepository;
  }

  @PostMapping
  public ApiResponse<?> submitFeedback(@RequestBody Map<String, String> payload) {
    Long userId = CurrentUserUtil.getCurrentUserId();
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      return ApiResponse.error(404, "用户不存在");
    }

    String type = payload.getOrDefault("type", "FEEDBACK");
    String content = payload.getOrDefault("content", "");
    if (content.trim().isEmpty()) {
      return ApiResponse.error(400, "反馈内容不能为空");
    }

    Feedback feedback = new Feedback();
    feedback.setUserId(userId);
    feedback.setEmail(user.getEmail());
    feedback.setNickname(user.getNickname());
    feedback.setType(type);
    feedback.setContent(content.trim());
    feedback.setStatus(0);

    feedbackRepository.save(feedback);
    return ApiResponse.success(Map.of("id", feedback.getId(), "status", feedback.getStatus()));
  }
}
