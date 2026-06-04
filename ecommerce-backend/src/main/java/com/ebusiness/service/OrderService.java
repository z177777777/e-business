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
import com.ebusiness.repository.ProductRepository;
import com.ebusiness.repository.UserRepository;
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
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
      CartItemRepository cartItemRepository, ProductRepository productRepository,
      UserRepository userRepository) {
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
    this.cartItemRepository = cartItemRepository;
    this.productRepository = productRepository;
    this.userRepository = userRepository;
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
    return listOrdersByUserId(userId);
  }

  public List<OrderResponse> listOrdersByUserId(Long userId) {
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
    boolean justPaid = false;
    if (OrderEntity.STATUS_PENDING.equals(order.getStatus())) {
      order.setStatus(OrderEntity.STATUS_PAID);
      order.setPaidAt(LocalDateTime.now());
      orderRepository.save(order);
      justPaid = true;
    }
    if (justPaid) {
      incrementSoldCount(order);
    }
    return toResponse(order, null);
  }

  private void incrementSoldCount(OrderEntity order) {
    List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
    for (OrderItem item : items) {
      if (item.getProductId() != null) {
        productRepository.findById(item.getProductId()).ifPresent(p -> {
          p.setSold(p.getSold() != null ? p.getSold() + item.getQuantity() : item.getQuantity());
          if (p.getStock() != null && p.getStock() >= item.getQuantity()) {
            p.setStock(p.getStock() - item.getQuantity());
          }
          productRepository.save(p);
        });
      }
    }
  }

  public java.util.List<OrderResponse> listAllOrders() {
    java.util.List<OrderResponse> responses = new java.util.ArrayList<>();
    for (OrderEntity order : orderRepository.findAll()) {
      responses.add(toResponse(order, null));
    }
    return responses;
  }

  public OrderResponse getOrderByAdmin(Long id) {
    OrderEntity order = orderRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "order not found"));
    return toResponse(order, null);
  }

  @Transactional
  public void deleteOrderByAdmin(Long id) {
    OrderEntity order = orderRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "order not found"));
    orderItemRepository.deleteByOrderId(order.getId());
    orderRepository.delete(order);
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
    String userName = userRepository.findById(order.getUserId())
        .map(u -> (u.getNickname() != null && !u.getNickname().trim().isEmpty()) ? u.getNickname() : u.getEmail())
        .orElse(null);
    return new OrderResponse(
      order.getId(),
      order.getOrderNo(),
      order.getTotalAmount(),
      order.getStatus(),
      order.getCreatedAt(),
      order.getPaidAt(),
      order.getShippedAt(),
      order.getReceivedAt(),
      order.getUpdatedAt(),
      order.getUserId(),
      userName,
      items);
  }

  @Transactional
  public OrderResponse receiveOrder(Long id) {
    Long userId = CurrentUserUtil.getCurrentUserId();
    OrderEntity order = orderRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "order not found"));
    if (!OrderEntity.STATUS_SHIPPED.equals(order.getStatus())) {
      throw new BusinessException(ErrorCode.BIZ_ERROR, "订单未发货，无法确认收货");
    }
    order.setStatus(OrderEntity.STATUS_RECEIVED);
    order.setReceivedAt(LocalDateTime.now());
    orderRepository.save(order);
    return toResponse(order, null);
  }

  @Transactional
  public void cancelOrder(Long id) {
    Long userId = CurrentUserUtil.getCurrentUserId();
    OrderEntity order = orderRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "order not found"));
    if (!OrderEntity.STATUS_PENDING.equals(order.getStatus())) {
      throw new BusinessException(ErrorCode.BIZ_ERROR, "订单已支付或已发货，无法取消");
    }
    order.setStatus(OrderEntity.STATUS_CANCELLED);
    orderRepository.save(order);
  }

  @Transactional
  public void requestRefund(Long id) {
    Long userId = CurrentUserUtil.getCurrentUserId();
    OrderEntity order = orderRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "order not found"));
    if (!OrderEntity.STATUS_PAID.equals(order.getStatus())) {
      throw new BusinessException(ErrorCode.BIZ_ERROR, "订单未支付或已发货，无法申请退款");
    }
    order.setStatus(OrderEntity.STATUS_REFUND_REQUESTED);
    orderRepository.save(order);
  }

  @Transactional
  public void approveRefund(Long id) {
    OrderEntity order = orderRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "order not found"));
    if (!OrderEntity.STATUS_REFUND_REQUESTED.equals(order.getStatus())) {
      throw new BusinessException(ErrorCode.BIZ_ERROR, "订单未申请退款，无法确认退款");
    }
    // 恢复库存和销量
    restoreStock(order);
    order.setStatus(OrderEntity.STATUS_REFUNDED);
    orderRepository.save(order);
  }

  private void restoreStock(OrderEntity order) {
    List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
    for (OrderItem item : items) {
      if (item.getProductId() != null) {
        productRepository.findById(item.getProductId()).ifPresent(p -> {
          if (p.getSold() != null && p.getSold() >= item.getQuantity()) {
            p.setSold(p.getSold() - item.getQuantity());
          }
          if (p.getStock() != null) {
            p.setStock(p.getStock() + item.getQuantity());
          }
          productRepository.save(p);
        });
      }
    }
  }
}
