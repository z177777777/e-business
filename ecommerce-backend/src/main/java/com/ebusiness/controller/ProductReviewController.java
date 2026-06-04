package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.dto.review.ProductReviewItemResponse;
import com.ebusiness.dto.review.ProductReviewListResponse;
import com.ebusiness.dto.review.ProductReviewRequest;
import com.ebusiness.service.ProductReviewService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/{idOrSlug}/reviews")
public class ProductReviewController {
  private final ProductReviewService productReviewService;

  public ProductReviewController(ProductReviewService productReviewService) {
    this.productReviewService = productReviewService;
  }

  @GetMapping
  public ApiResponse<ProductReviewListResponse> list(@PathVariable String idOrSlug) {
    return ApiResponse.success(productReviewService.listReviews(idOrSlug));
  }

  @PostMapping
  public ApiResponse<ProductReviewItemResponse> submit(@PathVariable String idOrSlug,
      @Valid @RequestBody ProductReviewRequest request) {
    return ApiResponse.success(productReviewService.submitReview(idOrSlug, request));
  }
}
