package com.ebusiness.common;

public enum ErrorCode {
  SUCCESS(0, "OK"),
  INVALID_PARAM(4001, "Invalid parameter"),
  EMAIL_CODE_INVALID(4002, "Email code invalid"),
  EMAIL_CODE_EXPIRED(4003, "Email code expired"),
  USER_EXISTS(4004, "User already exists"),
  USER_NOT_FOUND(4005, "User not found"),
  PASSWORD_ERROR(4006, "Password incorrect"),
  USER_DISABLED(4007, "User disabled"),
  RATE_LIMIT(4290, "Request too frequent"),
  UNAUTHORIZED(4010, "Unauthorized"),
  FORBIDDEN(4030, "Forbidden"),
  NOT_FOUND(4040, "Not found"),
  BIZ_ERROR(5001, "Business error"),
  SERVER_ERROR(5000, "Server error");

  private final int code;
  private final String message;

  ErrorCode(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
