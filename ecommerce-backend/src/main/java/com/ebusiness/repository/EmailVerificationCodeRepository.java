package com.ebusiness.repository;

import com.ebusiness.entity.EmailVerificationCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
  List<EmailVerificationCode> findTop5ByEmailAndPurposeOrderByCreatedAtDesc(String email, String purpose);
}
