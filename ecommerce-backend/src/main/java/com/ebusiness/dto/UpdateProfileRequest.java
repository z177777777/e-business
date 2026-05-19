package com.ebusiness.dto;

import javax.validation.constraints.Size;

public class UpdateProfileRequest {
  @Size(max = 64, message = "Nickname too long")
  private String nickname;

  @Size(max = 255, message = "Avatar url too long")
  private String avatarUrl;

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }
}
