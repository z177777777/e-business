package com.ebusiness.repository;

import com.ebusiness.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Page<Product> findByIsHotTrue(Pageable pageable);

  Page<Product> findByIsNewTrue(Pageable pageable);

  Optional<Product> findBySlug(String slug);
}
