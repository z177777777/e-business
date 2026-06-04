package com.ebusiness.repository;

import com.ebusiness.entity.Feedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
  List<Feedback> findTop20ByStatusOrderByCreatedAtDesc(Integer status);
}
