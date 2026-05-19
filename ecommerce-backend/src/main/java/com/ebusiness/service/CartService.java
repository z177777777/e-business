package com.ebusiness.service;

import com.ebusiness.common.BusinessException;
import com.ebusiness.common.CurrentUserUtil;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.dto.cart.AddCartItemRequest;
import com.ebusiness.dto.cart.CartItemResponse;
import com.ebusiness.dto.cart.CartListResponse;
import com.ebusiness.dto.cart.UpdateCartItemRequest;
import com.ebusiness.entity.CartItem;
import com.ebusiness.entity.Product;
import com.ebusiness.repository.CartItemRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
  private final CartItemRepository cartItemRepository;
  private final ProductService productService;

  public CartService(CartItemRepository cartItemRepository, ProductService productService) {
    this.cartItemRepository = cartItemRepository;
    this.productService = productService;
  }

  public CartListResponse getMyCart() {
    Long userId = CurrentUserUtil.getCurrentUserId();
    return buildCartListResponse(cartItemRepository.findByUserIdOrderByCreatedAtAsc(userId));
  }

  @Transactional
  public CartListResponse addItem(AddCartItemRequest request) {
    Long userId = CurrentUserUtil.getCurrentUserId();
    Product product = getProductByIdentifier(request.getProductIdentifier());
    int quantity = request.getQuantity() == null ? 1 : Math.max(1, request.getQuantity());
    CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, product.getId()).orElseGet(CartItem::new);
    cartItem.setUserId(userId);
    cartItem.setProductId(product.getId());
    cartItem.setProductIdentifier(product.getSlug() != null ? product.getSlug() : String.valueOf(product.getId()));
    cartItem.setProductName(product.getName());
    cartItem.setProductSubtitle(product.getSubtitle());
    cartItem.setProductCoverUrl(product.getCoverUrl());
    cartItem.setProductPrice(product.getPrice());
    cartItem.setQuantity((cartItem.getQuantity() == null ? 0 : cartItem.getQuantity()) + quantity);
    cartItem.setSelected(true);
    cartItemRepository.save(cartItem);
    return getMyCart();
  }

  @Transactional
  public CartListResponse updateItem(Long id, UpdateCartItemRequest request) {
    CartItem cartItem = getMyCartItem(id);
    if (request.getQuantity() != null) {
      if (request.getQuantity() < 1) {
        throw new BusinessException(ErrorCode.INVALID_PARAM, "quantity must be at least 1");
      }
      cartItem.setQuantity(request.getQuantity());
    }
    if (request.getSelected() != null) {
      cartItem.setSelected(request.getSelected());
    }
    cartItemRepository.save(cartItem);
    return getMyCart();
  }

  @Transactional
  public CartListResponse deleteItem(Long id) {
    CartItem cartItem = getMyCartItem(id);
    cartItemRepository.delete(cartItem);
    return getMyCart();
  }

  @Transactional
  public CartListResponse clearSelectedItems() {
    Long userId = CurrentUserUtil.getCurrentUserId();
    cartItemRepository.deleteByUserIdAndSelectedTrue(userId);
    return getMyCart();
  }

  private Product getProductByIdentifier(String productIdentifier) {
    Product product = productService.getByIdentifier(productIdentifier);
    if (product == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND, "product not found");
    }
    return product;
  }

  private CartItem getMyCartItem(Long id) {
    Long userId = CurrentUserUtil.getCurrentUserId();
    CartItem cartItem = cartItemRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "cart item not found"));
    if (!userId.equals(cartItem.getUserId())) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    return cartItem;
  }

  private CartListResponse buildCartListResponse(List<CartItem> cartItems) {
    List<CartItemResponse> items = new ArrayList<>();
    int totalQuantity = 0;
    int selectedQuantity = 0;
    BigDecimal selectedAmount = BigDecimal.ZERO;
    for (CartItem cartItem : cartItems) {
      BigDecimal price = cartItem.getProductPrice() == null ? BigDecimal.ZERO : cartItem.getProductPrice();
      int quantity = cartItem.getQuantity() == null ? 0 : cartItem.getQuantity();
      BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
      if (Boolean.TRUE.equals(cartItem.getSelected())) {
        selectedQuantity += quantity;
        selectedAmount = selectedAmount.add(subtotal);
      }
      totalQuantity += quantity;
      items.add(new CartItemResponse(
          cartItem.getId(),
          cartItem.getProductIdentifier(),
          cartItem.getProductId(),
          cartItem.getProductName(),
          cartItem.getProductSubtitle(),
          cartItem.getProductCoverUrl(),
          price,
          quantity,
          cartItem.getSelected(),
          subtotal));
    }
    return new CartListResponse(items, totalQuantity, selectedQuantity, selectedAmount);
  }
}
