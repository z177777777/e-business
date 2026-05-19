package com.ebusiness.service;

import com.ebusiness.entity.Product;
import com.ebusiness.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public Page<Product> listProducts(String type, int page, int size) {
    Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
    if ("hot".equalsIgnoreCase(type)) {
      return productRepository.findByIsHotTrue(pageable);
    }
    if ("new".equalsIgnoreCase(type)) {
      return productRepository.findByIsNewTrue(pageable);
    }
    return productRepository.findAll(pageable);
  }

  public Product getById(Long id) {
    return productRepository.findById(id).orElse(null);
  }

  public Product getByIdentifier(String idOrSlug) {
    if (idOrSlug == null) {
      return null;
    }
    try {
      Long id = Long.valueOf(idOrSlug);
      return getById(id);
    } catch (NumberFormatException ex) {
      return productRepository.findBySlug(idOrSlug).orElse(null);
    }
  }
}
