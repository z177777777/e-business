package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.dto.AuthLoginRequest;
import com.ebusiness.dto.AuthRegisterRequest;
import com.ebusiness.dto.AuthResponse;
import com.ebusiness.dto.ResetPasswordRequest;
import com.ebusiness.dto.SendCodeRequest;
import com.ebusiness.service.AuthService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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

  @PostMapping("/logout")
  public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
    authService.logout(token);
    return ApiResponse.success("Logged out", null);
  }
}
