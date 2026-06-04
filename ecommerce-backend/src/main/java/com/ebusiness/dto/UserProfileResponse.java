package com.ebusiness.dto;

public class UserProfileResponse {
  private Long id;
  private String email;
  private String nickname;
  private String avatarUrl;
  private String role;

  public UserProfileResponse(Long id, String email, String nickname, String avatarUrl, String role) {
    this.id = id;
    this.email = email;
    this.nickname = nickname;
    this.avatarUrl = avatarUrl;
    this.role = role;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getNickname() {
    return nickname;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public String getRole() {
    return role;
  }
}
