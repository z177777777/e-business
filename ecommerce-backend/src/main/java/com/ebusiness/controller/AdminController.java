package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.common.BusinessException;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.config.UserPrincipal;
import com.ebusiness.dto.admin.AdminDashboardStatsResponse;
import com.ebusiness.entity.CsrApplication;
import com.ebusiness.entity.Feedback;
import com.ebusiness.entity.PasswordResetRequest;
import com.ebusiness.dto.order.OrderResponse;
import com.ebusiness.entity.User;
import com.ebusiness.entity.OrderEntity;
import com.ebusiness.entity.Product;
import com.ebusiness.entity.ProductReview;
import com.ebusiness.repository.CsrApplicationRepository;
import com.ebusiness.repository.FeedbackRepository;
import com.ebusiness.repository.OrderRepository;
import com.ebusiness.repository.PasswordResetRequestRepository;
import com.ebusiness.repository.ProductRepository;
import com.ebusiness.repository.ProductReviewRepository;
import com.ebusiness.repository.UserRepository;
import com.ebusiness.service.MailService;
import com.ebusiness.service.OrderService;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AdminController.class);
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;
  private final OrderService orderService;
  private final StringRedisTemplate redisTemplate;
  private final PasswordEncoder passwordEncoder;
  private final PasswordResetRequestRepository passwordResetRequestRepository;
  private final ProductReviewRepository productReviewRepository;
  private final FeedbackRepository feedbackRepository;
  private final CsrApplicationRepository csrApplicationRepository;
  private final MailService mailService;

  public AdminController(UserRepository userRepository, ProductRepository productRepository,
      OrderRepository orderRepository, OrderService orderService, StringRedisTemplate redisTemplate,
      PasswordEncoder passwordEncoder, PasswordResetRequestRepository passwordResetRequestRepository,
      ProductReviewRepository productReviewRepository, FeedbackRepository feedbackRepository,
      CsrApplicationRepository csrApplicationRepository, MailService mailService) {
    this.userRepository = userRepository;
    this.productRepository = productRepository;
    this.orderRepository = orderRepository;
    this.orderService = orderService;
    this.redisTemplate = redisTemplate;
    this.passwordEncoder = passwordEncoder;
    this.passwordResetRequestRepository = passwordResetRequestRepository;
    this.productReviewRepository = productReviewRepository;
    this.feedbackRepository = feedbackRepository;
    this.csrApplicationRepository = csrApplicationRepository;
    this.mailService = mailService;
  }

  private void ensureAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserPrincipal)) throw new BusinessException(ErrorCode.UNAUTHORIZED);
    String email = ((UserPrincipal) principal).getUsername();
    if (!"admin@local".equalsIgnoreCase(email)) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
  }

  @GetMapping("/users")
  public ApiResponse<List<?>> listUsers() {
    ensureAdmin();
    List<User> users = userRepository.findAll();
    List<?> dto = users.stream().map(u -> new java.util.HashMap<String, Object>() {{
      put("id", u.getId()); put("email", u.getEmail()); put("nickname", u.getNickname()); put("avatarUrl", u.getAvatarUrl()); put("status", u.getStatus()); put("lastLoginAt", u.getLastLoginAt());
    }}).collect(Collectors.toList());
    return ApiResponse.success(dto);
  }

  @PostMapping("/users/{id}/status")
  public ApiResponse<?> setUserStatus(@PathVariable Long id, @RequestParam int status) {
    ensureAdmin();
    User u = userRepository.findById(id).orElse(null);
    if (u == null) return ApiResponse.error(404, "user not found");
    u.setStatus(status);
    userRepository.save(u);
    return ApiResponse.success(new java.util.HashMap<String, Object>() {{ put("id", u.getId()); put("status", u.getStatus()); }});
  }

  @PutMapping("/users/{id}")
  public ApiResponse<?> updateUser(@PathVariable Long id, @RequestBody java.util.Map<String, Object> payload) {
    ensureAdmin();
    User u = userRepository.findById(id).orElse(null);
    if (u == null) return ApiResponse.error(404, "user not found");

    Object emailObj = payload.get("email");
    if (emailObj != null) {
      String email = String.valueOf(emailObj).trim();
      if (!email.isEmpty() && !email.equalsIgnoreCase(u.getEmail())) {
        if (userRepository.existsByEmail(email)) return ApiResponse.error(400, "email already exists");
        u.setEmail(email);
      }
    }

    Object nicknameObj = payload.get("nickname");
    if (nicknameObj != null) {
      String nickname = String.valueOf(nicknameObj).trim();
      if (!nickname.isEmpty()) u.setNickname(nickname);
    }

    Object avatarObj = payload.get("avatarUrl");
    if (avatarObj != null) {
      String avatarUrl = String.valueOf(avatarObj).trim();
      u.setAvatarUrl(avatarUrl.isEmpty() ? null : avatarUrl);
    }

    Object statusObj = payload.get("status");
    if (statusObj != null) {
      int status;
      try {
        status = Integer.parseInt(String.valueOf(statusObj));
      } catch (Exception e) {
        return ApiResponse.error(400, "invalid status");
      }
      if (status != 0 && status != 1) return ApiResponse.error(400, "invalid status");
      u.setStatus(status);
    }

    userRepository.save(u);
    return ApiResponse.success(new java.util.HashMap<String, Object>() {{
      put("id", u.getId());
      put("email", u.getEmail());
      put("nickname", u.getNickname());
      put("avatarUrl", u.getAvatarUrl());
      put("status", u.getStatus());
      put("lastLoginAt", u.getLastLoginAt());
    }});
  }

  @PostMapping("/users/{id}/reset-password")
  public ApiResponse<?> resetUserPassword(@PathVariable Long id, @RequestBody java.util.Map<String, Object> payload) {
    ensureAdmin();
    User u = userRepository.findById(id).orElse(null);
    if (u == null) return ApiResponse.error(404, "user not found");

    String newPassword = payload == null ? null : String.valueOf(payload.getOrDefault("newPassword", "")).trim();
    if (newPassword == null || newPassword.length() < 6) {
      return ApiResponse.error(400, "newPassword length must be at least 6");
    }

    u.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(u);
    return ApiResponse.success(new java.util.HashMap<String, Object>() {{ put("id", u.getId()); put("reset", true); }});
  }

  @DeleteMapping("/users/{id}")
  public ApiResponse<?> deleteUserById(@PathVariable Long id) {
    ensureAdmin();
    if (!userRepository.existsById(id)) return ApiResponse.error(404, "user not found");
    userRepository.deleteById(id);
    return ApiResponse.success(null);
  }

  @DeleteMapping("/users/by-email")
  public ApiResponse<?> deleteUserByEmail(@RequestParam String email) {
    ensureAdmin();
    java.util.Optional<User> uOpt = userRepository.findByEmail(email);
    if (!uOpt.isPresent()) return ApiResponse.error(404, "user not found");
    userRepository.delete(uOpt.get());
    return ApiResponse.success(null);
  }

  @GetMapping("/orders")
  public ApiResponse<List<OrderResponse>> listOrders() {
    ensureAdmin();
    return ApiResponse.success(orderService.listAllOrders());
  }

  @GetMapping("/orders/{id}")
  public ApiResponse<OrderResponse> getOrder(@PathVariable Long id) {
    ensureAdmin();
    logger.info("Admin requested order id={}", id);
    OrderResponse resp = orderService.getOrderByAdmin(id);
    logger.info("Admin found order id={} orderNo={}", id, resp.getOrderNo());
    return ApiResponse.success(resp);
  }

  @DeleteMapping("/orders/{id}")
  public ApiResponse<?> deleteOrder(@PathVariable Long id) {
    ensureAdmin();
    orderService.deleteOrderByAdmin(id);
    return ApiResponse.success(null);
  }

  @PostMapping("/orders/{id}/ship")
  public ApiResponse<?> shipOrder(@PathVariable Long id) {
    ensureAdmin();
    OrderEntity order = orderRepository.findById(id).orElse(null);
    if (order == null) return ApiResponse.error(404, "order not found");
    if (!OrderEntity.STATUS_PAID.equals(order.getStatus())) {
      return ApiResponse.error(400, "订单未支付，无法发货");
    }
    order.setStatus(OrderEntity.STATUS_SHIPPED);
    order.setShippedAt(LocalDateTime.now());
    orderRepository.save(order);
    return ApiResponse.success(new java.util.HashMap<String, Object>() {{ put("id", order.getId()); put("status", order.getStatus()); put("shippedAt", order.getShippedAt()); }});
  }

  @GetMapping("/password-reset-requests")
  public ApiResponse<List<?>> listPasswordResetRequests(@RequestParam(required = false) Integer status) {
    ensureAdmin();
    List<PasswordResetRequest> list = status == null
        ? passwordResetRequestRepository.findAll()
        : passwordResetRequestRepository.findTop10ByStatusOrderByCreatedAtDesc(status);
    List<?> dto = list.stream().map(r -> new java.util.HashMap<String, Object>() {{
      put("id", r.getId());
      put("email", r.getEmail());
      put("note", r.getNote());
      put("status", r.getStatus());
      put("createdAt", r.getCreatedAt());
    }}).collect(Collectors.toList());
    return ApiResponse.success(dto);
  }

  @PostMapping("/password-reset-requests/{id}/handled")
  public ApiResponse<?> markPasswordResetHandled(@PathVariable Long id) {
    ensureAdmin();
    PasswordResetRequest request = passwordResetRequestRepository.findById(id).orElse(null);
    if (request == null) return ApiResponse.error(404, "request not found");
    request.setStatus(1);
    passwordResetRequestRepository.save(request);
    return ApiResponse.success(new java.util.HashMap<String, Object>() {{ put("id", request.getId()); put("status", request.getStatus()); }});
  }

  @PostMapping("/password-reset-requests/handled-by-email")
  public ApiResponse<?> markPasswordResetHandledByEmail(@RequestParam String email) {
    ensureAdmin();
    if (email == null || email.trim().isEmpty()) return ApiResponse.error(400, "email is required");
    List<PasswordResetRequest> requests = passwordResetRequestRepository.findByEmailAndStatusOrderByCreatedAtDesc(email.trim().toLowerCase(java.util.Locale.ROOT), 0);
    for (PasswordResetRequest request : requests) {
      request.setStatus(1);
    }
    passwordResetRequestRepository.saveAll(requests);
    return ApiResponse.success(new java.util.HashMap<String, Object>() {{ put("handledCount", requests.size()); }});
  }

  @GetMapping("/recent-activities")
  public ApiResponse<List<?>> recentActivities(@RequestParam(defaultValue = "8") int limit) {
    ensureAdmin();
    int safeLimit = Math.max(1, Math.min(limit, 20));

    List<Map<String, Object>> merged = new ArrayList<>();

    List<User> users = userRepository.findTop5ByEmailNotOrderByCreatedAtDesc("admin@local");
    for (User u : users) {
      if (u.getCreatedAt() == null) continue;
      String who = (u.getNickname() != null && !u.getNickname().trim().isEmpty()) ? u.getNickname() : u.getEmail();
      merged.add(new java.util.HashMap<String, Object>() {{
        put("time", u.getCreatedAt());
        put("text", "用户「" + who + "」完成注册");
      }});
    }

    List<Product> products = productRepository.findTop5ByOrderByCreatedAtDesc();
    for (Product p : products) {
      if (p.getCreatedAt() == null) continue;
      merged.add(new java.util.HashMap<String, Object>() {{
        put("time", p.getCreatedAt());
        put("text", "新增商品「" + p.getName() + "」");
      }});
    }

    List<OrderEntity> orders = orderRepository.findTop5ByOrderByCreatedAtDesc();
    for (OrderEntity o : orders) {
      if (o.getCreatedAt() == null) continue;
      User orderUser = userRepository.findById(o.getUserId()).orElse(null);
      String orderUserName = (orderUser != null && orderUser.getNickname() != null && !orderUser.getNickname().trim().isEmpty())
          ? orderUser.getNickname() : (orderUser != null ? orderUser.getEmail() : "未知用户");
      merged.add(new java.util.HashMap<String, Object>() {{
        put("time", o.getCreatedAt());
        put("text", orderUserName + " 提交了订单「" + o.getOrderNo() + "」");
      }});
    }

    List<PasswordResetRequest> requests = passwordResetRequestRepository.findTop5ByOrderByCreatedAtDesc();
    for (PasswordResetRequest r : requests) {
      if (r.getCreatedAt() == null) continue;
      merged.add(new java.util.HashMap<String, Object>() {{
        put("time", r.getCreatedAt());
        put("text", r.getEmail() + " 提交了密码重置请求");
      }});
    }

    merged.sort((a, b) -> {
      LocalDateTime at = (LocalDateTime) a.get("time");
      LocalDateTime bt = (LocalDateTime) b.get("time");
      if (at == null && bt == null) return 0;
      if (at == null) return 1;
      if (bt == null) return -1;
      return bt.compareTo(at);
    });

    List<?> result = merged.stream().limit(safeLimit).map(m -> new java.util.HashMap<String, Object>() {{
      put("time", m.get("time"));
      put("text", m.get("text"));
    }}).collect(Collectors.toList());

    return ApiResponse.success(result);
  }

  @GetMapping("/products")
  public ApiResponse<List<?>> listAllProducts() {
    ensureAdmin();
    List<com.ebusiness.entity.Product> list = productRepository.findAll();

    // Batch-fetch all reviews and group by productId for efficient counting
    List<ProductReview> allReviews = productReviewRepository.findAll();
    Map<Long, List<ProductReview>> reviewsByProduct = allReviews.stream()
        .collect(Collectors.groupingBy(ProductReview::getProductId));

    List<?> dto = list.stream().map(p -> {
      List<ProductReview> pReviews = reviewsByProduct.getOrDefault(p.getId(), Collections.emptyList());
      boolean hasReviews = !pReviews.isEmpty();
      boolean hasNewReviews = false;
      if (hasReviews && p.getReviewLastViewedAt() != null) {
        hasNewReviews = pReviews.stream().anyMatch(r -> r.getCreatedAt().isAfter(p.getReviewLastViewedAt()));
      } else if (hasReviews && p.getReviewLastViewedAt() == null) {
        hasNewReviews = true;
      }
      final boolean fHasReviews = hasReviews;
      final boolean fHasNewReviews = hasNewReviews;
      return new java.util.HashMap<String, Object>() {{
        put("id", p.getId()); put("name", p.getName()); put("category", p.getCategory()); put("price", p.getPrice()); put("isPublished", p.getIsPublished()); put("slug", p.getSlug()); put("stock", p.getStock()); put("restockRequests", p.getRestockRequests());
        put("hasReviews", fHasReviews); put("hasNewReviews", fHasNewReviews);
      }};
    }).collect(Collectors.toList());
    return ApiResponse.success(dto);
  }

  @PostMapping("/products")
  public ApiResponse<?> createProduct(@org.springframework.web.bind.annotation.RequestBody com.ebusiness.entity.Product payload) {
    ensureAdmin();
    if (payload.getIsPublished() == null) payload.setIsPublished(true);
    if (payload.getStock() == null) payload.setStock(10);
    if (payload.getRestockRequests() == null) payload.setRestockRequests(0);
    if (payload.getSold() == null) payload.setSold(0);
    if (payload.getSlug() == null || payload.getSlug().trim().isEmpty()) payload.setSlug(null);
    com.ebusiness.entity.Product saved = productRepository.save(payload);
    return ApiResponse.success(saved);
  }

  @PutMapping("/products/{id}")
  public ApiResponse<?> updateProduct(@org.springframework.web.bind.annotation.PathVariable Long id,
      @org.springframework.web.bind.annotation.RequestBody com.ebusiness.entity.Product payload) {
    ensureAdmin();
    com.ebusiness.entity.Product p = productRepository.findById(id).orElse(null);
    if (p == null) return ApiResponse.error(404, "product not found");
    // copy allowed fields
    p.setName(payload.getName()); p.setSubtitle(payload.getSubtitle()); p.setDescription(payload.getDescription()); p.setPrice(payload.getPrice()); p.setCoverUrl(payload.getCoverUrl()); p.setCategory(payload.getCategory());
    String slug = payload.getSlug();
    p.setSlug(slug == null || slug.trim().isEmpty() ? null : slug.trim());
    p.setIsHot(payload.getIsHot()); p.setIsNew(payload.getIsNew()); p.setIsPublished(payload.getIsPublished());
    if (payload.getStock() != null) p.setStock(payload.getStock());
    if (payload.getRestockRequests() != null) p.setRestockRequests(payload.getRestockRequests());
    productRepository.save(p);
    return ApiResponse.success(p);
  }

  @DeleteMapping("/products/{idOrSlug}")
  public ApiResponse<?> deleteProduct(@org.springframework.web.bind.annotation.PathVariable("idOrSlug") String idOrSlug) {
    ensureAdmin();
    // try parse as numeric id first
    try {
      Long id = Long.parseLong(idOrSlug);
      if (!productRepository.existsById(id)) return ApiResponse.error(404, "product not found");
      productRepository.deleteById(id);
      return ApiResponse.success(null);
    } catch (NumberFormatException nfe) {
      // treat as slug
      java.util.Optional<com.ebusiness.entity.Product> pOpt = productRepository.findBySlug(idOrSlug);
      if (!pOpt.isPresent()) return ApiResponse.error(404, "product not found");
      productRepository.delete(pOpt.get());
      return ApiResponse.success(null);
    }
  }

  @PostMapping("/products/{id}/publish")
  public ApiResponse<?> setProductPublish(@org.springframework.web.bind.annotation.PathVariable Long id,
      @org.springframework.web.bind.annotation.RequestParam boolean published) {
    ensureAdmin();
    com.ebusiness.entity.Product p = productRepository.findById(id).orElse(null);
    if (p == null) return ApiResponse.error(404, "product not found");
    p.setIsPublished(published);
    productRepository.save(p);
    return ApiResponse.success(p);
  }

  @PostMapping("/orders/{id}/refund-approve")
  public ApiResponse<?> approveRefund(@PathVariable Long id) {
    ensureAdmin();
    OrderEntity order = orderRepository.findById(id).orElse(null);
    if (order == null) return ApiResponse.error(404, "order not found");
    orderService.approveRefund(id);
    // 退款到用户钱包
    User refundUser = userRepository.findById(order.getUserId()).orElse(null);
    if (refundUser != null && order.getTotalAmount() != null) {
      java.math.BigDecimal bal = refundUser.getWalletBalance() != null ? refundUser.getWalletBalance() : java.math.BigDecimal.ZERO;
      refundUser.setWalletBalance(bal.add(order.getTotalAmount()));
      userRepository.save(refundUser);
    }
    return ApiResponse.success(null);
  }

  @GetMapping("/dashboard-stats")
  public ApiResponse<AdminDashboardStatsResponse> dashboardStats() {
    ensureAdmin();
    long userCount = userRepository.countByEmailNot("admin@local");
    long productCount = productRepository.count();
    long orderCount = orderRepository.count();
    long pendingOrderCount = orderRepository.countByStatus(OrderEntity.STATUS_PENDING);
    long pendingPasswordResetRequestCount = passwordResetRequestRepository.countByStatus(0);
    long pvToday = userRepository.countByLastLoginAtBetween(
        java.time.LocalDate.now().atStartOfDay(),
        java.time.LocalDate.now().plusDays(1).atStartOfDay());
    return ApiResponse.success(new AdminDashboardStatsResponse(userCount, productCount, orderCount, pendingOrderCount, pendingPasswordResetRequestCount, pvToday));
  }

  @GetMapping("/chart-data")
  public ApiResponse<Map<String, Object>> chartData(@RequestParam(defaultValue = "7") int days) {
    ensureAdmin();
    int safeDays = Math.max(1, Math.min(days, 365));
    LocalDateTime start = LocalDate.now().atStartOfDay().minusDays(safeDays - 1);
    LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

    Map<String, Object> result = new LinkedHashMap<>();

    // 1. 订单状态分布 (LinkedHashMap 保持中文 key 顺序)
    LinkedHashMap<String, Long> orderStatusDistribution = new LinkedHashMap<>();
    orderStatusDistribution.put("待付款", orderRepository.countByStatus(OrderEntity.STATUS_PENDING));
    orderStatusDistribution.put("已付款", orderRepository.countByStatus(OrderEntity.STATUS_PAID));
    orderStatusDistribution.put("已发货", orderRepository.countByStatus(OrderEntity.STATUS_SHIPPED));
    orderStatusDistribution.put("已收货", orderRepository.countByStatus(OrderEntity.STATUS_RECEIVED));
    orderStatusDistribution.put("已取消", orderRepository.countByStatus(OrderEntity.STATUS_CANCELLED));
    orderStatusDistribution.put("退款中", orderRepository.countByStatus(OrderEntity.STATUS_REFUND_REQUESTED));
    orderStatusDistribution.put("已退款", orderRepository.countByStatus(OrderEntity.STATUS_REFUNDED));
    result.put("orderStatusDistribution", orderStatusDistribution);

    // 2. 商品分类分布
    List<Product> allProducts = productRepository.findAll();
    Map<String, Long> productCategoryDistribution = allProducts.stream()
        .collect(Collectors.groupingBy(p -> p.getCategory() != null ? p.getCategory() : "未分类", Collectors.counting()));
    result.put("productCategoryDistribution", productCategoryDistribution);

    // 3. 订单趋势 & 4. 销售额趋势
    List<OrderEntity> recentOrders = orderRepository.findByCreatedAtBetween(start, end);

    List<String> orderTrendLabels = new ArrayList<>();
    List<Long> orderTrendValues = new ArrayList<>();
    List<String> salesTrendLabels = new ArrayList<>();
    List<java.math.BigDecimal> salesTrendValues = new ArrayList<>();

    for (int i = safeDays - 1; i >= 0; i--) {
      LocalDate date = LocalDate.now().minusDays(i);
      String label = date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd"));
      orderTrendLabels.add(label);
      salesTrendLabels.add(label);

      LocalDateTime dayStart = date.atStartOfDay();
      LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

      final LocalDateTime ds = dayStart;
      final LocalDateTime de = dayEnd;
      long dayOrders = recentOrders.stream()
          .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(ds) && o.getCreatedAt().isBefore(de))
          .count();
      java.math.BigDecimal daySales = recentOrders.stream()
          .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(ds) && o.getCreatedAt().isBefore(de))
          .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : java.math.BigDecimal.ZERO)
          .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

      orderTrendValues.add(dayOrders);
      salesTrendValues.add(daySales);
    }

    LinkedHashMap<String, Object> orderTrend = new LinkedHashMap<>();
    orderTrend.put("labels", orderTrendLabels);
    orderTrend.put("values", orderTrendValues);
    result.put("orderTrend", orderTrend);

    LinkedHashMap<String, Object> salesTrend = new LinkedHashMap<>();
    salesTrend.put("labels", salesTrendLabels);
    salesTrend.put("values", salesTrendValues);
    result.put("salesTrend", salesTrend);

    // 5. KPI 仪表盘：订单履约率 + 退款率
    long shippedCount = orderRepository.countByStatus(OrderEntity.STATUS_SHIPPED);
    long receivedCount = orderRepository.countByStatus(OrderEntity.STATUS_RECEIVED);
    long paidCount = orderRepository.countByStatus(OrderEntity.STATUS_PAID);
    long refundReqCount = orderRepository.countByStatus(OrderEntity.STATUS_REFUND_REQUESTED);
    long refundedCount = orderRepository.countByStatus(OrderEntity.STATUS_REFUNDED);
    long meaningfulOrders = shippedCount + receivedCount + paidCount + refundReqCount + refundedCount;
    double fulfillmentRate = meaningfulOrders > 0
        ? Math.round((shippedCount + receivedCount) * 10000.0 / meaningfulOrders) / 100.0 : 0.0;
    double refundRate = meaningfulOrders > 0
        ? Math.round((refundReqCount + refundedCount) * 10000.0 / meaningfulOrders) / 100.0 : 0.0;
    LinkedHashMap<String, Double> kpiGauge = new LinkedHashMap<>();
    kpiGauge.put("fulfillmentRate", fulfillmentRate);
    kpiGauge.put("refundRate", refundRate);
    result.put("kpiGauge", kpiGauge);

    // 6. 订单流转漏斗
    LinkedHashMap<String, Long> orderFunnel = new LinkedHashMap<>();
    orderFunnel.put("待付款", orderRepository.countByStatus(OrderEntity.STATUS_PENDING));
    orderFunnel.put("已付款", paidCount);
    orderFunnel.put("已发货", shippedCount);
    orderFunnel.put("已收货", receivedCount);
    result.put("orderFunnel", orderFunnel);

    // 7. 日历热力图：近90天每日订单量
    LocalDateTime heatmapStart = LocalDate.now().minusDays(89).atStartOfDay();
    LocalDateTime heatmapEnd = LocalDate.now().plusDays(1).atStartOfDay();
    List<OrderEntity> heatmapOrders = orderRepository.findByCreatedAtBetween(heatmapStart, heatmapEnd);
    Map<LocalDate, Long> heatmapMap = heatmapOrders.stream()
        .filter(o -> o.getCreatedAt() != null)
        .collect(Collectors.groupingBy(
            o -> o.getCreatedAt().toLocalDate(),
            Collectors.counting()));
    List<String[]> calendarHeatmap = heatmapMap.entrySet().stream()
        .map(e -> new String[]{e.getKey().toString(), String.valueOf(e.getValue())})
        .collect(Collectors.toList());
    result.put("calendarHeatmap", calendarHeatmap);

    return ApiResponse.success(result);
  }

  // ---- Review management ----

  @PostMapping("/products/{productId}/mark-reviews-viewed")
  public ApiResponse<?> markReviewsViewed(@PathVariable Long productId) {
    ensureAdmin();
    com.ebusiness.entity.Product p = productRepository.findById(productId).orElse(null);
    if (p == null) return ApiResponse.error(404, "product not found");
    p.setReviewLastViewedAt(LocalDateTime.now());
    productRepository.save(p);
    return ApiResponse.success(null);
  }

  @GetMapping("/products/{productId}/reviews")
  public ApiResponse<List<?>> listProductReviews(@PathVariable Long productId) {
    ensureAdmin();
    List<ProductReview> reviews = productReviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    List<?> dto = reviews.stream().map(r -> {
      User revUser = userRepository.findById(r.getUserId()).orElse(null);
      return new java.util.HashMap<String, Object>() {{
        put("id", r.getId());
        put("productId", r.getProductId());
        put("userId", r.getUserId());
        put("nickname", revUser != null ? revUser.getNickname() : "匿名用户");
        put("rating", r.getRating());
        put("content", r.getContent());
        put("imageUrls", r.getImageUrls());
        put("createdAt", r.getCreatedAt());
      }};
    }).collect(java.util.stream.Collectors.toList());
    return ApiResponse.success(dto);
  }

  @DeleteMapping("/reviews/{id}")
  public ApiResponse<?> deleteReview(@PathVariable Long id) {
    ensureAdmin();
    if (!productReviewRepository.existsById(id)) return ApiResponse.error(404, "review not found");
    productReviewRepository.deleteById(id);
    return ApiResponse.success(null);
  }

  @DeleteMapping("/products/{productId}/reviews")
  public ApiResponse<?> deleteProductReviews(@PathVariable Long productId) {
    ensureAdmin();
    List<ProductReview> reviews = productReviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    productReviewRepository.deleteAll(reviews);
    return ApiResponse.success(new java.util.HashMap<String, Object>() {{ put("deleted", reviews.size()); }});
  }

  // ---- All reviews ----

  @GetMapping("/reviews")
  public ApiResponse<List<?>> listAllReviews() {
    ensureAdmin();
    List<ProductReview> all = productReviewRepository.findAllByOrderByCreatedAtDesc();
    List<?> dto = all.stream().map(r -> {
      User revUser = userRepository.findById(r.getUserId()).orElse(null);
      Product revProduct = productRepository.findById(r.getProductId()).orElse(null);
      return new java.util.HashMap<String, Object>() {{
        put("id", r.getId());
        put("productId", r.getProductId());
        put("productName", revProduct != null ? revProduct.getName() : "未知商品");
        put("userId", r.getUserId());
        put("nickname", revUser != null ? revUser.getNickname() : "匿名用户");
        put("rating", r.getRating());
        put("content", r.getContent());
        put("imageUrls", r.getImageUrls());
        put("createdAt", r.getCreatedAt());
      }};
    }).collect(java.util.stream.Collectors.toList());
    return ApiResponse.success(dto);
  }

  // ---- Restock requests ----

  @GetMapping("/restock-requests")
  public ApiResponse<List<?>> listRestockRequests() {
    ensureAdmin();
    List<Product> all = productRepository.findAll();
    List<?> dto = all.stream()
        .filter(p -> p.getRestockRequests() != null && p.getRestockRequests() > 0)
        .map(p -> new java.util.HashMap<String, Object>() {{
          put("id", p.getId());
          put("name", p.getName());
          put("stock", p.getStock());
          put("restockRequests", p.getRestockRequests());
        }}).collect(java.util.stream.Collectors.toList());
    return ApiResponse.success(dto);
  }

  // ---- Unified user messages ----

  @GetMapping("/user-messages")
  public ApiResponse<List<?>> listUserMessages() {
    ensureAdmin();
    List<Map<String, Object>> messages = new ArrayList<>();

    // Password reset requests
    List<PasswordResetRequest> resetRequests = passwordResetRequestRepository.findTop10ByStatusOrderByCreatedAtDesc(0);
    for (PasswordResetRequest r : resetRequests) {
      messages.add(new java.util.HashMap<String, Object>() {{
        put("type", "PASSWORD_RESET");
        put("id", r.getId());
        put("title", r.getEmail());
        put("note", r.getNote() != null ? r.getNote() : "请求密码重置");
        put("createdAt", r.getCreatedAt());
        put("handled", false);
        put("target", r.getEmail());
      }});
    }

    // Product reviews (recent 10)
    List<ProductReview> allReviews = productReviewRepository.findAllByOrderByCreatedAtDesc();
    java.util.stream.Stream<ProductReview> reviewStream = allReviews.stream().limit(10);
    reviewStream.forEach(r -> {
      User revUser = userRepository.findById(r.getUserId()).orElse(null);
      Product revProduct = productRepository.findById(r.getProductId()).orElse(null);
      messages.add(new java.util.HashMap<String, Object>() {{
        put("type", "REVIEW");
        put("id", r.getId());
        put("title", revProduct != null ? revProduct.getName() : "未知商品");
        put("nickname", revUser != null ? revUser.getNickname() : "匿名用户");
        put("note", r.getContent() != null ? r.getContent() : "");
        put("createdAt", r.getCreatedAt());
        put("handled", false);
        put("productId", r.getProductId());
        put("reviewId", r.getId());
        put("rating", r.getRating());
      }});
    });

    // Restock requests
    List<Product> allProducts = productRepository.findAll();
    for (Product p : allProducts) {
      if (p.getRestockRequests() != null && p.getRestockRequests() > 0) {
        messages.add(new java.util.HashMap<String, Object>() {{
          put("type", "RESTOCK_REQUEST");
          put("id", p.getId());
          put("title", p.getName());
          put("note", p.getRestockRequests() + " 人催上货（库存: " + (p.getStock() != null ? p.getStock() : 0) + "）");
          put("createdAt", p.getUpdatedAt());
          put("handled", false);
          put("productId", p.getId());
        }});
      }
    }

    // User feedbacks (unread)
    List<Feedback> feedbacks = feedbackRepository.findTop20ByStatusOrderByCreatedAtDesc(0);
    for (Feedback f : feedbacks) {
      messages.add(new java.util.HashMap<String, Object>() {{
        put("type", f.getType());
        put("id", f.getId());
        put("title", f.getNickname() != null ? f.getNickname() : f.getEmail());
        put("nickname", f.getNickname() != null ? f.getNickname() : f.getEmail());
        put("note", f.getContent());
        put("createdAt", f.getCreatedAt());
        put("handled", false);
        put("email", f.getEmail());
      }});
    }

    messages.sort((a, b) -> {
      Object at = a.get("createdAt");
      Object bt = b.get("createdAt");
      if (at == null && bt == null) return 0;
      if (at == null) return 1;
      if (bt == null) return -1;
      return ((java.time.LocalDateTime) bt).compareTo((java.time.LocalDateTime) at);
    });

    return ApiResponse.success(messages);
  }

  @PostMapping("/feedback/{id}/read")
  public ApiResponse<?> markFeedbackRead(@PathVariable Long id) {
    ensureAdmin();
    Feedback feedback = feedbackRepository.findById(id).orElse(null);
    if (feedback == null) return ApiResponse.error(404, "feedback not found");
    feedback.setStatus(1);
    feedbackRepository.save(feedback);
    return ApiResponse.success(new java.util.HashMap<String, Object>() {{ put("id", feedback.getId()); put("status", feedback.getStatus()); }});
  }

  // --- CSR Applications ---

  @GetMapping("/csr-applications")
  public ApiResponse<List<Map<String, Object>>> listCsrApplications() {
    ensureAdmin();
    List<CsrApplication> applications = csrApplicationRepository.findAllByOrderByCreatedAtDesc();
    List<Map<String, Object>> result = applications.stream().map(a -> {
      Map<String, Object> map = new java.util.LinkedHashMap<>();
      map.put("id", a.getId());
      map.put("userId", a.getUserId());
      map.put("email", a.getEmail());
      map.put("nickname", a.getNickname());
      map.put("note", a.getNote());
      map.put("status", a.getStatus());
      map.put("createdAt", a.getCreatedAt());
      map.put("updatedAt", a.getUpdatedAt());
      return map;
    }).collect(Collectors.toList());
    return ApiResponse.success(result);
  }

  @PostMapping("/csr-applications/{id}/approve")
  public ApiResponse<?> approveCsrApplication(@PathVariable Long id) {
    ensureAdmin();
    CsrApplication application = csrApplicationRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Application not found"));
    if (application.getStatus() != 0) {
      throw new BusinessException(ErrorCode.BIZ_ERROR, "Application already processed");
    }

    application.setStatus(1);
    csrApplicationRepository.save(application);

    // If user already exists, upgrade role to CSR
    if (application.getUserId() != null) {
      User user = userRepository.findById(application.getUserId()).orElse(null);
      if (user != null) {
        user.setRole("CSR");
        userRepository.save(user);
      }
    }

    // Send approval email
    try {
      mailService.sendCsrApproval(application.getEmail());
    } catch (Exception e) {
      logger.warn("Failed to send CSR approval email to {}: {}", application.getEmail(), e.getMessage());
    }

    Map<String, Object> result = new java.util.HashMap<>();
    result.put("id", application.getId());
    result.put("status", 1);
    result.put("message", "Approved successfully");
    return ApiResponse.success(result);
  }

  @PostMapping("/csr-applications/{id}/reject")
  public ApiResponse<?> rejectCsrApplication(@PathVariable Long id) {
    ensureAdmin();
    CsrApplication application = csrApplicationRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Application not found"));
    if (application.getStatus() != 0) {
      throw new BusinessException(ErrorCode.BIZ_ERROR, "Application already processed");
    }
    application.setStatus(2);
    csrApplicationRepository.save(application);

    Map<String, Object> result = new java.util.HashMap<>();
    result.put("id", application.getId());
    result.put("status", 2);
    result.put("message", "Rejected");
    return ApiResponse.success(result);
  }
}
