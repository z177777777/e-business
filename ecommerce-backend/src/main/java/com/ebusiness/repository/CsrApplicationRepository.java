package com.ebusiness.repository;

import com.ebusiness.entity.CsrApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsrApplicationRepository extends JpaRepository<CsrApplication, Long> {

  List<CsrApplication> findByStatusOrderByCreatedAtDesc(Integer status);

  List<CsrApplication> findAllByOrderByCreatedAtDesc();

  boolean existsByUserIdAndStatus(Long userId, Integer status);

  List<CsrApplication> findByEmailAndStatus(String email, Integer status);

  boolean existsByEmailAndStatusAndCreatedAtAfter(String email, Integer status, java.time.LocalDateTime after);
}
