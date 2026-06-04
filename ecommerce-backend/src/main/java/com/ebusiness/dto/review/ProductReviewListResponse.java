package com.ebusiness.dto.review;

import java.util.List;

public class ProductReviewListResponse {
  private final List<ProductReviewItemResponse> reviews;
  private final boolean canReview;

  public ProductReviewListResponse(List<ProductReviewItemResponse> reviews, boolean canReview) {
    this.reviews = reviews;
    this.canReview = canReview;
  }

  public List<ProductReviewItemResponse> getReviews() {
    return reviews;
  }

  public boolean isCanReview() {
    return canReview;
  }
}
