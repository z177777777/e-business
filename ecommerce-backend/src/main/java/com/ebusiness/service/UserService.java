package com.ebusiness.service;

import com.ebusiness.common.BusinessException;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.config.UserPrincipal;
import com.ebusiness.dto.UpdateEmailRequest;
import com.ebusiness.dto.UpdatePasswordRequest;
import com.ebusiness.dto.UpdateProfileRequest;
import com.ebusiness.dto.UserProfileResponse;
import com.ebusiness.entity.User;
import com.ebusiness.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final VerificationCodeService verificationCodeService;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository,
                     VerificationCodeService verificationCodeService,
                     PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.verificationCodeService = verificationCodeService;
    this.passwordEncoder = passwordEncoder;
  }

  public UserProfileResponse getProfile() {
    User user = getCurrentUser();
    return new UserProfileResponse(user.getId(), user.getEmail(), user.getNickname(), user.getAvatarUrl(), user.getRole());
  }

  public UserProfileResponse updateProfile(UpdateProfileRequest request) {
    User user = getCurrentUser();
    if (request.getNickname() != null) {
      user.setNickname(request.getNickname());
    }
    if (request.getAvatarUrl() != null) {
      user.setAvatarUrl(request.getAvatarUrl());
    }
    userRepository.save(user);
    return new UserProfileResponse(user.getId(), user.getEmail(), user.getNickname(), user.getAvatarUrl(), user.getRole());
  }

  public UserProfileResponse updateEmail(UpdateEmailRequest request) {
    User user = getCurrentUser();
    String newEmail = request.getNewEmail();
    if (userRepository.existsByEmail(newEmail)) {
      throw new BusinessException(ErrorCode.USER_EXISTS);
    }
    verificationCodeService.verifyCode(newEmail, "CHANGE_EMAIL", request.getCode());
    user.setEmail(newEmail);
    userRepository.save(user);
    return new UserProfileResponse(user.getId(), user.getEmail(), user.getNickname(), user.getAvatarUrl(), user.getRole());
  }

  public void updatePassword(UpdatePasswordRequest request) {
    User user = getCurrentUser();
    if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
      throw new BusinessException(ErrorCode.PASSWORD_ERROR);
    }
    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  private User getCurrentUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof UserPrincipal)) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    Long userId = ((UserPrincipal) principal).getId();
    return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }
}
