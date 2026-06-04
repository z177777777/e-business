package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.common.BusinessException;
import com.ebusiness.common.CurrentUserUtil;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.dto.order.OrderResponse;
import com.ebusiness.service.OrderService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
  public ApiResponse<List<OrderResponse>> list(@RequestParam(required = false) Long userId) {
    if (userId != null) {
      String role = CurrentUserUtil.getCurrentUserRole();
      if (!"CSR".equals(role)) {
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
      }
      return ApiResponse.success(orderService.listOrdersByUserId(userId));
    }
    return ApiResponse.success(orderService.listMyOrders());
  }

  @GetMapping("/{id}")
  public ApiResponse<OrderResponse> detail(@PathVariable Long id) {
    String role = CurrentUserUtil.getCurrentUserRole();
    if ("CSR".equals(role)) {
      return ApiResponse.success(orderService.getOrderByAdmin(id));
    }
    return ApiResponse.success(orderService.getMyOrder(id));
  }

  @PostMapping("/{id}/pay")
  public ApiResponse<OrderResponse> pay(@PathVariable Long id) {
    return ApiResponse.success(orderService.payOrder(id));
  }

  @PostMapping("/{id}/receive")
  public ApiResponse<OrderResponse> receive(@PathVariable Long id) {
    return ApiResponse.success(orderService.receiveOrder(id));
  }

  @PostMapping("/{id}/cancel")
  public ApiResponse<?> cancel(@PathVariable Long id) {
    orderService.cancelOrder(id);
    return ApiResponse.success(null);
  }

  @PostMapping("/{id}/refund")
  public ApiResponse<?> refund(@PathVariable Long id) {
    orderService.requestRefund(id);
    return ApiResponse.success(null);
  }
}
