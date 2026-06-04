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
      Page<Product> hotProducts = productRepository.findByIsHotTrueAndIsPublishedTrue(pageable);
      if (hotProducts.hasContent()) {
        return hotProducts;
      }
      // 如果当前没有手动标记的热销商品，按真实销量从已发布商品中选取
      return productRepository.findByIsPublishedTrueOrderBySoldDesc(pageable);
    }
    if ("new".equalsIgnoreCase(type)) {
      return productRepository.findByIsNewTrueAndIsPublishedTrue(pageable);
    }
    // only return published products for public listing
    return productRepository.findByIsPublishedTrue(pageable);
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
