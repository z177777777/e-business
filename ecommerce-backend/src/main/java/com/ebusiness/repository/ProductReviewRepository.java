package com.ebusiness.repository;

import com.ebusiness.entity.ProductReview;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
  List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);

  Optional<ProductReview> findByProductIdAndUserId(Long productId, Long userId);

  List<ProductReview> findAllByOrderByCreatedAtDesc();
}
