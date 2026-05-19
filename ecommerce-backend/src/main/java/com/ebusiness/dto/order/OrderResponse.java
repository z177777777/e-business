package com.ebusiness.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
  private Long id;
  private String orderNo;
  private BigDecimal totalAmount;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime paidAt;
  private List<OrderItemResponse> items;

  public OrderResponse(Long id, String orderNo, BigDecimal totalAmount, String status, LocalDateTime createdAt,
      LocalDateTime paidAt, List<OrderItemResponse> items) {
    this.id = id;
    this.orderNo = orderNo;
    this.totalAmount = totalAmount;
    this.status = status;
    this.createdAt = createdAt;
    this.paidAt = paidAt;
    this.items = items;
  }

  public Long getId() {
    return id;
  }

  public String getOrderNo() {
    return orderNo;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public String getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getPaidAt() {
    return paidAt;
  }

  public List<OrderItemResponse> getItems() {
    return items;
  }
}