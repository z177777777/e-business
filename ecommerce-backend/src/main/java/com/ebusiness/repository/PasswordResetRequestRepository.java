package com.ebusiness.repository;

import com.ebusiness.entity.PasswordResetRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {
  long countByStatus(Integer status);

  List<PasswordResetRequest> findTop10ByStatusOrderByCreatedAtDesc(Integer status);

  List<PasswordResetRequest> findTop5ByOrderByCreatedAtDesc();

  List<PasswordResetRequest> findByEmailAndStatusOrderByCreatedAtDesc(String email, Integer status);
}
