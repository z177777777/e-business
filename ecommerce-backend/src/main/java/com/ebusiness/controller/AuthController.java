package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.dto.AuthLoginRequest;
import com.ebusiness.dto.AuthRegisterRequest;
import com.ebusiness.dto.AuthResponse;
import com.ebusiness.dto.PasswordResetSupportRequest;
import com.ebusiness.dto.ResetPasswordRequest;
import com.ebusiness.dto.SendCodeRequest;
import com.ebusiness.service.AuthService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/send-code")
  public ApiResponse<Void> sendCode(@Valid @RequestBody SendCodeRequest request, HttpServletRequest httpRequest) {
    String ip = httpRequest.getRemoteAddr();
    authService.sendCode(request, ip);
    return ApiResponse.success("Code sent", null);
  }

  @PostMapping("/register")
  public ApiResponse<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
    return ApiResponse.success(authService.register(request));
  }

  @PostMapping("/login")
  public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthLoginRequest request) {
    return ApiResponse.success(authService.login(request));
  }

  @PostMapping("/reset-password")
  public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ApiResponse.success("Password updated", null);
  }

  @PostMapping("/password-reset-request")
  public ApiResponse<Void> requestPasswordResetSupport(@Valid @RequestBody PasswordResetSupportRequest request) {
    authService.requestPasswordResetSupport(request);
    return ApiResponse.success("Password reset request submitted", null);
  }

  @PostMapping("/apply-csr")
  public ApiResponse<Void> applyCsr(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    if (email == null || email.trim().isEmpty()) {
      return ApiResponse.error(4001, "请提供邮箱");
    }
    authService.applyCsr(email);
    return ApiResponse.success("已联系管理员，请等待邮箱通知", null);
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
    authService.logout(token);
    return ApiResponse.success("Logged out", null);
  }
}
