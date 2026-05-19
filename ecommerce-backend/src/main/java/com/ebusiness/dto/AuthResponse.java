package com.ebusiness.dto;

public class AuthResponse {
  private String token;
  private long expireAt;
  private UserProfileResponse user;

  public AuthResponse(String token, long expireAt, UserProfileResponse user) {
    this.token = token;
    this.expireAt = expireAt;
    this.user = user;
  }

  public String getToken() {
    return token;
  }

  public long getExpireAt() {
    return expireAt;
  }

  public UserProfileResponse getUser() {
    return user;
  }
}
