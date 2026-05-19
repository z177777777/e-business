package com.ebusiness.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class SendCodeRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Email is invalid")
  private String email;

  @NotBlank(message = "Purpose is required")
  private String purpose;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPurpose() {
    return purpose;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }
}
