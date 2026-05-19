package com.ebusiness.dto.cart;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class AddCartItemRequest {
  @NotBlank(message = "商品标识不能为空")
  private String productIdentifier;

  @Min(value = 1, message = "数量至少为1")
  private Integer quantity = 1;

  public String getProductIdentifier() {
    return productIdentifier;
  }

  public void setProductIdentifier(String productIdentifier) {
    this.productIdentifier = productIdentifier;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }
}