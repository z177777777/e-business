package com.ebusiness.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class UpdateEmailRequest {
  @NotBlank(message = "New email is required")
  @Email(message = "Email is invalid")
  private String newEmail;

  @NotBlank(message = "Code is required")
  private String code;

  public String getNewEmail() {
    return newEmail;
  }

  public void setNewEmail(String newEmail) {
    this.newEmail = newEmail;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
