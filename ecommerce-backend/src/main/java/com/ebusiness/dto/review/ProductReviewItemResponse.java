package com.ebusiness.dto.review;

import java.time.LocalDateTime;

public class ProductReviewItemResponse {
  private final Long id;
  private final Long productId;
  private final Long userId;
  private final String nickname;
  private final String avatarUrl;
  private final Integer rating;
  private final String content;
  private final java.util.List<String> imageUrls;
  private final LocalDateTime createdAt;

  public ProductReviewItemResponse(Long id, Long productId, Long userId, String nickname, String avatarUrl,
      Integer rating, String content, java.util.List<String> imageUrls, LocalDateTime createdAt) {
    this.id = id;
    this.productId = productId;
    this.userId = userId;
    this.nickname = nickname;
    this.avatarUrl = avatarUrl;
    this.rating = rating;
    this.content = content;
    this.imageUrls = imageUrls;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Long getProductId() {
    return productId;
  }

  public Long getUserId() {
    return userId;
  }

  public String getNickname() {
    return nickname;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public Integer getRating() {
    return rating;
  }

  public String getContent() {
    return content;
  }

  public java.util.List<String> getImageUrls() {
    return imageUrls;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
