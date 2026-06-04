package com.ebusiness.repository;

import com.ebusiness.entity.OrderEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
  List<OrderEntity> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
  List<OrderEntity> findTop5ByOrderByCreatedAtDesc();
  Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);
  long countByStatus(String status);
  List<OrderEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
