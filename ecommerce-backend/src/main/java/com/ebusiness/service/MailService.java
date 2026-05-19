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
}
