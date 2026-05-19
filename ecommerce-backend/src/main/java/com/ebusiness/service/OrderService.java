package com.ebusiness.service;

import com.ebusiness.common.BusinessException;
import com.ebusiness.common.CurrentUserUtil;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.dto.order.OrderItemResponse;
import com.ebusiness.dto.order.OrderResponse;
import com.ebusiness.entity.CartItem;
import com.ebusiness.entity.OrderEntity;
import com.ebusiness.entity.OrderItem;
import com.ebusiness.repository.CartItemRepository;
import com.ebusiness.repository.OrderItemRepository;
import com.ebusiness.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final CartItemRepository cartItemRepository;

  public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
      CartItemRepository cartItemRepository) {
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
    this.cartItemRepository = cartItemRepository;
  }

  @Transactional
  public OrderResponse checkout() {
    Long userId = CurrentUserUtil.getCurrentUserId();
    List<CartItem> selectedItems = getSelectedCartItems(userId);
    if (selectedItems.isEmpty()) {
      throw new BusinessException(ErrorCode.BIZ_ERROR, "请选择要结算的商品");
    }

    BigDecimal totalAmount = BigDecimal.ZERO;
    for (CartItem cartItem : selectedItems) {
      totalAmount = totalAmount.add(safePrice(cartItem).multiply(BigDecimal.valueOf(safeQuantity(cartItem))));
    }

    OrderEntity order = new OrderEntity();
    order.setOrderNo(generateOrderNo());
    order.setUserId(userId);
    order.setTotalAmount(totalAmount);
    order.setStatus(OrderEntity.STATUS_PENDING);
    order = orderRepository.save(order);

    List<OrderItemResponse> itemResponses = new ArrayList<>();
    for (CartItem cartItem : selectedItems) {
      BigDecimal productPrice = safePrice(cartItem);
      int quantity = safeQuantity(cartItem);
      BigDecimal subtotal = productPrice.multiply(BigDecimal.valueOf(quantity));

      OrderItem orderItem = new OrderItem();
      orderItem.setOrderId(order.getId());
      orderItem.setProductId(cartItem.getProductId());
      orderItem.setProductIdentifier(cartItem.getProductIdentifier());
      orderItem.setProductName(cartItem.getProductName());
      orderItem.setProductSubtitle(cartItem.getProductSubtitle());
      orderItem.setProductCoverUrl(cartItem.getProductCoverUrl());
      orderItem.setProductPrice(productPrice);
      orderItem.setQuantity(quantity);
      orderItem.setSubtotal(subtotal);
      orderItem = orderItemRepository.save(orderItem);
      itemResponses.add(toResponse(orderItem));
    }

    cartItemRepository.deleteAll(selectedItems);
    return toResponse(order, itemResponses);
  }

  public List<OrderResponse> listMyOrders() {
    Long userId = CurrentUserUtil.getCurrentUserId();
    List<OrderResponse> responses = new ArrayList<>();
    for (OrderEntity order : orderRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
      responses.add(toResponse(order, null));
    }
    return responses;
  }

  public OrderResponse getMyOrder(Long id) {
    Long userId = CurrentUserUtil.getCurrentUserId();
    OrderEntity order = orderRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "order not found"));
    return toResponse(order, null);
  }

  @Transactional
  public OrderResponse payOrder(Long id) {
    Long userId = CurrentUserUtil.getCurrentUserId();
    OrderEntity order = orderRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "order not found"));
    if (OrderEntity.STATUS_PENDING.equals(order.getStatus())) {
      order.setStatus(OrderEntity.STATUS_PAID);
      order.setPaidAt(LocalDateTime.now());
      orderRepository.save(order);
    }
    return toResponse(order, null);
  }

  private List<CartItem> getSelectedCartItems(Long userId) {
    List<CartItem> selectedItems = new ArrayList<>();
    for (CartItem cartItem : cartItemRepository.findByUserIdOrderByCreatedAtAsc(userId)) {
      if (Boolean.TRUE.equals(cartItem.getSelected())) {
        selectedItems.add(cartItem);
      }
    }
    return selectedItems;
  }

  private String generateOrderNo() {
    return "OD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
  }

  private BigDecimal safePrice(CartItem cartItem) {
    return cartItem.getProductPrice() == null ? BigDecimal.ZERO : cartItem.getProductPrice();
  }

  private int safeQuantity(CartItem cartItem) {
    return cartItem.getQuantity() == null ? 0 : cartItem.getQuantity();
  }

  private OrderItemResponse toResponse(OrderItem orderItem) {
    return new OrderItemResponse(
        orderItem.getId(),
        orderItem.getProductIdentifier(),
        orderItem.getProductId(),
        orderItem.getProductName(),
        orderItem.getProductSubtitle(),
        orderItem.getProductCoverUrl(),
        orderItem.getProductPrice(),
        orderItem.getQuantity(),
        orderItem.getSubtotal());
  }

  private OrderResponse toResponse(OrderEntity order, List<OrderItemResponse> presetItems) {
    List<OrderItemResponse> items = presetItems;
    if (items == null) {
      items = new ArrayList<>();
      for (OrderItem orderItem : orderItemRepository.findByOrderIdOrderByIdAsc(order.getId())) {
        items.add(toResponse(orderItem));
      }
    }
    return new OrderResponse(
        order.getId(),
        order.getOrderNo(),
        order.getTotalAmount(),
        order.getStatus(),
        order.getCreatedAt(),
        order.getPaidAt(),
        items);
  }
}
