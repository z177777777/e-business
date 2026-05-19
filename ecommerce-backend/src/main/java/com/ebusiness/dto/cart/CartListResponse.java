package com.ebusiness.dto.cart;

import java.math.BigDecimal;
import java.util.List;

public class CartListResponse {
  private List<CartItemResponse> items;
  private Integer totalQuantity;
  private Integer selectedQuantity;
  private BigDecimal selectedAmount;

  public CartListResponse(List<CartItemResponse> items, Integer totalQuantity, Integer selectedQuantity,
      BigDecimal selectedAmount) {
    this.items = items;
    this.totalQuantity = totalQuantity;
    this.selectedQuantity = selectedQuantity;
    this.selectedAmount = selectedAmount;
  }

  public List<CartItemResponse> getItems() {
    return items;
  }

  public Integer getTotalQuantity() {
    return totalQuantity;
  }

  public Integer getSelectedQuantity() {
    return selectedQuantity;
  }

  public BigDecimal getSelectedAmount() {
    return selectedAmount;
  }
}