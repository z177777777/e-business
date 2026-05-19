package com.ebusiness.repository;

import com.ebusiness.entity.OrderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
  Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);
}
