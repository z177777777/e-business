package com.ebusiness.common;

public enum ErrorCode {
  SUCCESS(0, "成功"),
  INVALID_PARAM(4001, "参数无效"),
  EMAIL_CODE_INVALID(4002, "邮箱验证码无效"),
  EMAIL_CODE_EXPIRED(4003, "邮箱验证码已过期"),
  USER_EXISTS(4004, "用户已存在"),
  USER_NOT_FOUND(4005, "用户不存在"),
  PASSWORD_ERROR(4006, "密码错误"),
  USER_DISABLED(4007, "用户已被禁用"),
  RATE_LIMIT(4290, "请求过于频繁"),
  UNAUTHORIZED(4010, "未登录或会话已过期"),
  FORBIDDEN(4030, "无权限访问"),
  NOT_FOUND(4040, "未找到"),
  BIZ_ERROR(5001, "业务错误"),
  SERVER_ERROR(5000, "服务器错误");

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
