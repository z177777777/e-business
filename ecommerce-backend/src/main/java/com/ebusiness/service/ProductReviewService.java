package com.ebusiness.service;

import com.ebusiness.common.BusinessException;
import com.ebusiness.common.CurrentUserUtil;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.config.UserPrincipal;
import com.ebusiness.dto.review.ProductReviewItemResponse;
import com.ebusiness.dto.review.ProductReviewListResponse;
import com.ebusiness.dto.review.ProductReviewRequest;
import com.ebusiness.entity.OrderEntity;
import com.ebusiness.entity.OrderItem;
import com.ebusiness.entity.Product;
import com.ebusiness.entity.ProductReview;
import com.ebusiness.entity.User;
import com.ebusiness.repository.OrderItemRepository;
import com.ebusiness.repository.OrderRepository;
import com.ebusiness.repository.ProductRepository;
import com.ebusiness.repository.ProductReviewRepository;
import com.ebusiness.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductReviewService {
  private final ProductRepository productRepository;
  private final ProductReviewRepository productReviewRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ProductReviewService(ProductRepository productRepository,
      ProductReviewRepository productReviewRepository,
      OrderRepository orderRepository,
      OrderItemRepository orderItemRepository,
      UserRepository userRepository) {
    this.productRepository = productRepository;
    this.productReviewRepository = productReviewRepository;
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
    this.userRepository = userRepository;
  }

  public ProductReviewListResponse listReviews(String idOrSlug) {
    Product product = resolveProduct(idOrSlug);
    Long productId = product.getId();
    List<ProductReview> reviews = productReviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    boolean canReview = canCurrentUserReview(productId);
    return new ProductReviewListResponse(mapReviews(reviews), canReview);
  }

  @Transactional
  public ProductReviewItemResponse submitReview(String idOrSlug, ProductReviewRequest request) {
    Product product = resolveProduct(idOrSlug);
    Long userId = CurrentUserUtil.getCurrentUserId();
    if (!hasPurchasedProduct(userId, product.getId())) {
      throw new BusinessException(ErrorCode.FORBIDDEN, "Only purchased users can post reviews");
    }

    ProductReview review = new ProductReview();
    review.setProductId(product.getId());
    review.setUserId(userId);
    review.setRating(request.getRating());
    review.setContent(request.getContent().trim());
    try {
      if (request.getImageUrls() != null) {
        review.setImageUrls(objectMapper.writeValueAsString(request.getImageUrls()));
      } else {
        review.setImageUrls(null);
      }
    } catch (JsonProcessingException e) {
      throw new BusinessException(ErrorCode.SERVER_ERROR, "Failed to process image urls");
    }
    review = productReviewRepository.save(review);
    return toResponse(review);
  }

  public boolean canCurrentUserReview(Long productId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
      return false;
    }
    Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
    return hasPurchasedProduct(userId, productId);
  }

  private boolean hasPurchasedProduct(Long userId, Long productId) {
    List<OrderEntity> paidOrders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, OrderEntity.STATUS_RECEIVED);
    if (paidOrders.isEmpty()) {
      return false;
    }
    List<Long> orderIds = paidOrders.stream().map(OrderEntity::getId).collect(Collectors.toList());
    List<OrderItem> matchedItems = orderItemRepository.findByProductIdAndOrderIdIn(productId, orderIds);
    return !matchedItems.isEmpty();
  }

  private Product resolveProduct(String idOrSlug) {
    if (idOrSlug == null || idOrSlug.trim().isEmpty()) {
      throw new BusinessException(ErrorCode.INVALID_PARAM, "product id is required");
    }
    Product product;
    try {
      Long id = Long.valueOf(idOrSlug);
      product = productRepository.findById(id).orElse(null);
    } catch (NumberFormatException ex) {
      product = productRepository.findBySlug(idOrSlug).orElse(null);
    }
    if (product == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND, "product not found");
    }
    return product;
  }

  private List<ProductReviewItemResponse> mapReviews(List<ProductReview> reviews) {
    if (reviews.isEmpty()) {
      return Collections.emptyList();
    }
    Set<Long> userIds = new HashSet<>();
    for (ProductReview review : reviews) {
      userIds.add(review.getUserId());
    }
    Map<Long, User> userMap = new HashMap<>();
    for (User user : userRepository.findAllById(userIds)) {
      userMap.put(user.getId(), user);
    }
    List<ProductReviewItemResponse> result = new ArrayList<>();
    for (ProductReview review : reviews) {
      User user = userMap.get(review.getUserId());
      List<String> imgs = Collections.emptyList();
      try {
        if (review.getImageUrls() != null) {
          imgs = objectMapper.readValue(review.getImageUrls(), new TypeReference<List<String>>() {});
        }
      } catch (Exception ex) {
        imgs = Collections.emptyList();
      }
      result.add(new ProductReviewItemResponse(
          review.getId(),
          review.getProductId(),
          review.getUserId(),
          user != null ? user.getNickname() : "匿名用户",
          user != null ? user.getAvatarUrl() : null,
          review.getRating(),
          review.getContent(),
          imgs,
          review.getCreatedAt()));
    }
    return result;
  }

  private ProductReviewItemResponse toResponse(ProductReview review) {
    User user = userRepository.findById(review.getUserId()).orElse(null);
    List<String> imgs = Collections.emptyList();
    try {
      if (review.getImageUrls() != null) {
        imgs = objectMapper.readValue(review.getImageUrls(), new TypeReference<List<String>>() {});
      }
    } catch (Exception ex) {
      imgs = Collections.emptyList();
    }
    return new ProductReviewItemResponse(
        review.getId(),
        review.getProductId(),
        review.getUserId(),
        user != null ? user.getNickname() : "匿名用户",
        user != null ? user.getAvatarUrl() : null,
        review.getRating(),
        review.getContent(),
        imgs,
        review.getCreatedAt());
  }
}
