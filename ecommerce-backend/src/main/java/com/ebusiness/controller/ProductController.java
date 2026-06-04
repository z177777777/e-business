package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.common.BusinessException;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.entity.Product;
import com.ebusiness.repository.ProductRepository;
import com.ebusiness.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  private final ProductService productService;
  private final ProductRepository productRepository;

  public ProductController(ProductService productService, ProductRepository productRepository) {
    this.productService = productService;
    this.productRepository = productRepository;
  }

  @GetMapping
  public ApiResponse<Page<Product>> list(
      @RequestParam(required = false) String type,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<Product> result = productService.listProducts(type, page, size);
    return ApiResponse.success(result);
  }

  @GetMapping("/{idOrSlug}")
  public ApiResponse<Product> detail(@PathVariable("idOrSlug") String idOrSlug) {
    Product p = productService.getByIdentifier(idOrSlug);
    if (p == null) {
      return ApiResponse.error(404, "product not found");
    }
    return ApiResponse.success(p);
  }

  @PostMapping("/{id}/request-restock")
  public ApiResponse<?> requestRestock(@PathVariable Long id) {
    Product p = productRepository.findById(id).orElse(null);
    if (p == null) return ApiResponse.error(404, "product not found");
    int count = p.getRestockRequests() != null ? p.getRestockRequests() + 1 : 1;
    p.setRestockRequests(count);
    productRepository.save(p);
    return ApiResponse.success(new java.util.HashMap<String, Object>() {{
      put("restockRequests", count);
    }});
  }
}
