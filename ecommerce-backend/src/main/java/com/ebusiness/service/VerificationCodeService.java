package com.ebusiness.service;

import com.ebusiness.common.BusinessException;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.entity.EmailVerificationCode;
import com.ebusiness.repository.EmailVerificationCodeRepository;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class VerificationCodeService {
  private final StringRedisTemplate redisTemplate;
  private final MailService mailService;
  private final EmailVerificationCodeRepository codeRepository;

  @Value("${app.auth.codeExpireMinutes}")
  private long codeExpireMinutes;

  @Value("${app.auth.codeResendSeconds}")
  private long codeResendSeconds;

  public VerificationCodeService(
      StringRedisTemplate redisTemplate,
      MailService mailService,
      EmailVerificationCodeRepository codeRepository) {
    this.redisTemplate = redisTemplate;
    this.mailService = mailService;
    this.codeRepository = codeRepository;
  }

  public void sendCode(String email, String purpose, String ip) {
    String limitKey = "code:limit:" + purpose + ":" + email;
    Boolean allowed = redisTemplate.opsForValue().setIfAbsent(limitKey, "1", codeResendSeconds, TimeUnit.SECONDS);
    if (Boolean.FALSE.equals(allowed)) {
      throw new BusinessException(ErrorCode.RATE_LIMIT, "Please wait before requesting another code");
    }

    String code = generateCode();
    String codeKey = "code:" + purpose + ":" + email;
    redisTemplate.opsForValue().set(codeKey, code, codeExpireMinutes, TimeUnit.MINUTES);

    mailService.sendVerificationCode(email, code, purpose);

    EmailVerificationCode record = new EmailVerificationCode();
    record.setEmail(email);
    record.setCode(code);
    record.setPurpose(purpose);
    record.setExpiresAt(LocalDateTime.now().plusMinutes(codeExpireMinutes));
    record.setUsed(0);
    record.setSendIp(ip);
    codeRepository.save(record);
  }

  public void verifyCode(String email, String purpose, String code) {
    String codeKey = "code:" + purpose + ":" + email;
    String cached = redisTemplate.opsForValue().get(codeKey);
    if (cached == null) {
      throw new BusinessException(ErrorCode.EMAIL_CODE_EXPIRED);
    }
    if (!cached.equals(code)) {
      throw new BusinessException(ErrorCode.EMAIL_CODE_INVALID);
    }
    redisTemplate.delete(codeKey);
  }

  private String generateCode() {
    int value = new Random().nextInt(1000000);
    return String.format("%06d", value);
  }
}
