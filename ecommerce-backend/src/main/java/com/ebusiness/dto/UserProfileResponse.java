package com.ebusiness.dto;

public class UserProfileResponse {
  private Long id;
  private String email;
  private String nickname;
  private String avatarUrl;

  public UserProfileResponse(Long id, String email, String nickname, String avatarUrl) {
    this.id = id;
    this.email = email;
    this.nickname = nickname;
    this.avatarUrl = avatarUrl;
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
}
