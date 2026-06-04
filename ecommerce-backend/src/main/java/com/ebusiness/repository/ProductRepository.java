package com.ebusiness.repository;

import com.ebusiness.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Page<Product> findByIsHotTrueAndIsPublishedTrue(Pageable pageable);

  Page<Product> findByIsNewTrueAndIsPublishedTrue(Pageable pageable);

  Page<Product> findByIsPublishedTrueOrderBySoldDesc(Pageable pageable);

  Page<Product> findByIsPublishedTrue(Pageable pageable);

  Optional<Product> findBySlug(String slug);

  List<Product> findTop5ByOrderByCreatedAtDesc();
}
