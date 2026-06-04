package com.ebusiness.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailService {
  private final JavaMailSender mailSender;
  @Value("${spring.mail.username}")
  private String from;

  public MailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendVerificationCode(String to, String code, String purpose) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setSubject("Verification Code");
    message.setText("Your verification code is: " + code + ". Purpose: " + purpose);
    mailSender.send(message);
  }

  public void sendCsrApproval(String to) {
    String registerUrl = "http://localhost:5173/register?csr=true";
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setSubject("客服申请已通过");
    message.setText("你的客服申请已通过审核！\n\n请前往以下链接注册成为客服：\n" + registerUrl + "\n\n注册后即可使用客服功能。");
    mailSender.send(message);
  }
}
