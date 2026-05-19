package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.dto.order.OrderResponse;
import com.ebusiness.service.OrderService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping("/checkout")
  public ApiResponse<OrderResponse> checkout() {
    return ApiResponse.success(orderService.checkout());
  }

  @GetMapping
  public ApiResponse<List<OrderResponse>> list() {
    return ApiResponse.success(orderService.listMyOrders());
  }

  @GetMapping("/{id}")
  public ApiResponse<OrderResponse> detail(@PathVariable Long id) {
    return ApiResponse.success(orderService.getMyOrder(id));
  }

  @PostMapping("/{id}/pay")
  public ApiResponse<OrderResponse> pay(@PathVariable Long id) {
    return ApiResponse.success(orderService.payOrder(id));
  }
}
