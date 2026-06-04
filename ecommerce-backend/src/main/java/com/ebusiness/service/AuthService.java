package com.ebusiness.service;

import com.ebusiness.common.BusinessException;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.config.JwtUtil;
import com.ebusiness.dto.AuthLoginRequest;
import com.ebusiness.dto.AuthRegisterRequest;
import com.ebusiness.dto.AuthResponse;
import com.ebusiness.dto.ResetPasswordRequest;
import com.ebusiness.dto.SendCodeRequest;
import com.ebusiness.dto.PasswordResetSupportRequest;
import com.ebusiness.dto.UserProfileResponse;
import com.ebusiness.entity.CsrApplication;
import com.ebusiness.entity.User;
import com.ebusiness.entity.PasswordResetRequest;
import com.ebusiness.repository.CsrApplicationRepository;
import com.ebusiness.repository.UserRepository;
import com.ebusiness.repository.PasswordResetRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final VerificationCodeService verificationCodeService;
  private final JwtUtil jwtUtil;
  private final StringRedisTemplate redisTemplate;
  private final PasswordResetRequestRepository passwordResetRequestRepository;
  private final CsrApplicationRepository csrApplicationRepository;

  @Value("${app.jwt.expireMinutes}")
  private long expireMinutes;

  @Value("${app.jwt.rememberExpireDays}")
  private long rememberExpireDays;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      VerificationCodeService verificationCodeService,
      JwtUtil jwtUtil,
      StringRedisTemplate redisTemplate,
      PasswordResetRequestRepository passwordResetRequestRepository,
      CsrApplicationRepository csrApplicationRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.verificationCodeService = verificationCodeService;
    this.jwtUtil = jwtUtil;
    this.redisTemplate = redisTemplate;
    this.passwordResetRequestRepository = passwordResetRequestRepository;
    this.csrApplicationRepository = csrApplicationRepository;
  }

  public void sendCode(SendCodeRequest request, String ip) {
    String purpose = normalizePurpose(request.getPurpose());
    String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

    if (("REGISTER".equals(purpose) || "CHANGE_EMAIL".equals(purpose)) && userRepository.existsByEmail(email)) {
      throw new BusinessException(ErrorCode.USER_EXISTS);
    }
    if ("RESET".equals(purpose) && !userRepository.existsByEmail(email)) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    if ("REGISTER".equals(purpose)) {
      // Check if there's a pending CSR application (within 24h) → block
      LocalDateTime since = LocalDateTime.now().minusHours(24);
      boolean hasPendingCsr = csrApplicationRepository.existsByEmailAndStatusAndCreatedAtAfter(email, 0, since);
      if (hasPendingCsr) {
        throw new BusinessException(ErrorCode.BIZ_ERROR, "该邮箱已申请成为客服，无法注册为普通账户");
      }
    }

    verificationCodeService.sendCode(email, purpose, ip);
  }

  public void applyCsr(String email) {
    String normalized = email.trim().toLowerCase(Locale.ROOT);
    if (userRepository.existsByEmail(normalized)) {
      throw new BusinessException(ErrorCode.BIZ_ERROR, "该邮箱已有帐户，无法申请成为客服");
    }
    LocalDateTime since = LocalDateTime.now().minusHours(24);
    if (csrApplicationRepository.existsByEmailAndStatusAndCreatedAtAfter(normalized, 0, since)) {
      throw new BusinessException(ErrorCode.BIZ_ERROR, "该邮箱24小时内已申请，请耐心等待");
    }
    csrApplicationRepository.save(createCsrApplication(normalized, "用户申请成为客服"));
  }

  private CsrApplication createCsrApplication(String email, String note) {
    CsrApplication app = new CsrApplication();
    app.setEmail(email);
    app.setNickname(email.split("@")[0]);
    app.setNote(note);
    app.setStatus(0);
    return app;
  }

  public AuthResponse register(AuthRegisterRequest request) {
    String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
    if (userRepository.existsByEmail(email)) {
      throw new BusinessException(ErrorCode.USER_EXISTS);
    }
    verificationCodeService.verifyCode(email, "REGISTER", request.getCode());

    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setNickname(defaultNickname(request.getNickname()));

    // Check if there's an approved CSR application → register as CSR
    List<CsrApplication> approvedApps = csrApplicationRepository.findByEmailAndStatus(email, 1);
    if (!approvedApps.isEmpty()) {
      user.setRole("CSR");
    } else {
      user.setRole("USER");
    }

    user.setStatus(1);
    userRepository.save(user);

    // Link approved applications to this user
    for (CsrApplication app : approvedApps) {
      app.setUserId(user.getId());
      csrApplicationRepository.save(app);
    }

    return buildAuthResponse(user, false);
  }

  public AuthResponse login(AuthLoginRequest request) {
    String rawEmail = request.getEmail().trim();
    String email = rawEmail.toLowerCase(Locale.ROOT);
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    // 精确大小写比对：CSR@local 不能登录 csr@local 账号
    if (!rawEmail.equals(user.getEmail())) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    if (user.getStatus() == 0) {
      throw new BusinessException(ErrorCode.USER_DISABLED);
    }
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BusinessException(ErrorCode.PASSWORD_ERROR);
    }
    user.setLastLoginAt(LocalDateTime.now());
    userRepository.save(user);

    boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());
    return buildAuthResponse(user, rememberMe);
  }

  public void resetPassword(ResetPasswordRequest request) {
    String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
    verificationCodeService.verifyCode(email, "RESET", request.getCode());
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  public void requestPasswordResetSupport(PasswordResetSupportRequest request) {
    PasswordResetRequest supportRequest = new PasswordResetRequest();
    supportRequest.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
    String message = request.getMessage();
    if (message == null || message.trim().isEmpty()) {
      message = "用户在找回密码页面请求管理员协助重置密码";
    }
    supportRequest.setNote(message.trim());
    supportRequest.setStatus(0);
    passwordResetRequestRepository.save(supportRequest);
  }

  public void logout(String token) {
    if (token == null || !token.startsWith("Bearer ")) {
      return;
    }
    String raw = token.substring(7);
    try {
      String jti = jwtUtil.parseToken(raw).getId();
      redisTemplate.delete("session:" + jti);
    } catch (Exception ignored) {
      // Ignore invalid token
    }
  }

  private AuthResponse buildAuthResponse(User user, boolean rememberMe) {
    long minutes = rememberMe ? rememberExpireDays * 24 * 60 : expireMinutes;
    JwtUtil.TokenResult tokenResult = jwtUtil.generateToken(user.getId(), user.getEmail(), minutes);
    try {
      redisTemplate.opsForValue().set(
          "session:" + tokenResult.getJti(),
          String.valueOf(user.getId()),
          minutes,
          TimeUnit.MINUTES);
    } catch (Exception ex) {
      // 如果 Redis 不可用，记录警告并继续（不应阻止登录流程）
      System.err.println("Warning: failed to write session to Redis: " + ex.getMessage());
    }
    UserProfileResponse profile = new UserProfileResponse(user.getId(), user.getEmail(), user.getNickname(), user.getAvatarUrl(), user.getRole());
    return new AuthResponse(tokenResult.getToken(), tokenResult.getExpireAt(), profile);
  }

  private String normalizePurpose(String purpose) {
    if (purpose == null) {
      throw new BusinessException(ErrorCode.INVALID_PARAM, "Purpose is required");
    }
    String upper = purpose.trim().toUpperCase(Locale.ROOT);
    if (!"REGISTER".equals(upper) && !"RESET".equals(upper) && !"CHANGE_EMAIL".equals(upper)) {
      throw new BusinessException(ErrorCode.INVALID_PARAM, "Invalid purpose");
    }
    return upper;
  }

  private String defaultNickname(String nickname) {
    if (nickname != null && !nickname.trim().isEmpty()) {
      return nickname.trim();
    }
    return "user" + System.currentTimeMillis() % 100000;
  }
}
