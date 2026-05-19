package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.dto.cart.AddCartItemRequest;
import com.ebusiness.dto.cart.CartListResponse;
import com.ebusiness.dto.cart.UpdateCartItemRequest;
import com.ebusiness.service.CartService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping("/items")
  public ApiResponse<CartListResponse> getItems() {
    return ApiResponse.success(cartService.getMyCart());
  }

  @PostMapping("/items")
  public ApiResponse<CartListResponse> addItem(@Valid @RequestBody AddCartItemRequest request) {
    return ApiResponse.success(cartService.addItem(request));
  }

  @PutMapping("/items/{id}")
  public ApiResponse<CartListResponse> updateItem(@PathVariable Long id, @RequestBody UpdateCartItemRequest request) {
    return ApiResponse.success(cartService.updateItem(id, request));
  }

  @DeleteMapping("/items/{id}")
  public ApiResponse<CartListResponse> deleteItem(@PathVariable Long id) {
    return ApiResponse.success(cartService.deleteItem(id));
  }

  @DeleteMapping("/items")
  public ApiResponse<CartListResponse> clearSelectedItems() {
    return ApiResponse.success(cartService.clearSelectedItems());
  }
}
