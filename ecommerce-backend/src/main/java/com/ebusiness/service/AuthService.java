package com.ebusiness.service;

import com.ebusiness.common.BusinessException;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.config.JwtUtil;
import com.ebusiness.dto.AuthLoginRequest;
import com.ebusiness.dto.AuthRegisterRequest;
import com.ebusiness.dto.AuthResponse;
import com.ebusiness.dto.ResetPasswordRequest;
import com.ebusiness.dto.SendCodeRequest;
import com.ebusiness.dto.UserProfileResponse;
import com.ebusiness.entity.User;
import com.ebusiness.repository.UserRepository;
import java.time.LocalDateTime;
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

  @Value("${app.jwt.expireMinutes}")
  private long expireMinutes;

  @Value("${app.jwt.rememberExpireDays}")
  private long rememberExpireDays;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      VerificationCodeService verificationCodeService,
      JwtUtil jwtUtil,
      StringRedisTemplate redisTemplate) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.verificationCodeService = verificationCodeService;
    this.jwtUtil = jwtUtil;
    this.redisTemplate = redisTemplate;
  }

  public void sendCode(SendCodeRequest request, String ip) {
    String purpose = normalizePurpose(request.getPurpose());
    String email = request.getEmail();

    if (("REGISTER".equals(purpose) || "CHANGE_EMAIL".equals(purpose)) && userRepository.existsByEmail(email)) {
      throw new BusinessException(ErrorCode.USER_EXISTS);
    }
    if ("RESET".equals(purpose) && !userRepository.existsByEmail(email)) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    verificationCodeService.sendCode(email, purpose, ip);
  }

  public AuthResponse register(AuthRegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(ErrorCode.USER_EXISTS);
    }
    verificationCodeService.verifyCode(request.getEmail(), "REGISTER", request.getCode());

    User user = new User();
    user.setEmail(request.getEmail());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setNickname(defaultNickname(request.getNickname()));
    user.setStatus(1);
    userRepository.save(user);

    return buildAuthResponse(user, false);
  }

  public AuthResponse login(AuthLoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
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
    verificationCodeService.verifyCode(request.getEmail(), "RESET", request.getCode());
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
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
    redisTemplate.opsForValue().set(
        "session:" + tokenResult.getJti(),
        String.valueOf(user.getId()),
        minutes,
        TimeUnit.MINUTES);
    UserProfileResponse profile = new UserProfileResponse(user.getId(), user.getEmail(), user.getNickname(), user.getAvatarUrl());
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
