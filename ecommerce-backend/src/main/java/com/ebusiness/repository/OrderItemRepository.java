package com.ebusiness.repository;

import com.ebusiness.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  List<OrderItem> findByOrderIdOrderByIdAsc(Long orderId);
  void deleteByOrderId(Long orderId);

  List<OrderItem> findByProductIdAndOrderIdIn(Long productId, List<Long> orderIds);
}
