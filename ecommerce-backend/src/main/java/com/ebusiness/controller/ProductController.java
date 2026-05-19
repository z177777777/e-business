package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.entity.Product;
import com.ebusiness.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
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
}
