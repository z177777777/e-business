package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.dto.UpdateEmailRequest;
import com.ebusiness.dto.UpdatePasswordRequest;
import com.ebusiness.dto.UpdateProfileRequest;
import com.ebusiness.dto.UserProfileResponse;
import com.ebusiness.service.UserService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public ApiResponse<UserProfileResponse> me() {
    return ApiResponse.success(userService.getProfile());
  }

  @PutMapping("/me/profile")
  public ApiResponse<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
    return ApiResponse.success(userService.updateProfile(request));
  }

  @PutMapping("/me/email")
  public ApiResponse<UserProfileResponse> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
    return ApiResponse.success(userService.updateEmail(request));
  }

  @PutMapping("/me/password")
  public ApiResponse<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
    userService.updatePassword(request);
    return ApiResponse.success("Password updated", null);
  }
}
