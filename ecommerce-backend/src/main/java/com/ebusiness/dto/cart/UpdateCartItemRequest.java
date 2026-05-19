package com.ebusiness.dto.cart;

import javax.validation.constraints.Min;

public class UpdateCartItemRequest {
  @Min(value = 1, message = "数量至少为1")
  private Integer quantity;

  private Boolean selected;

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public Boolean getSelected() {
    return selected;
  }

  public void setSelected(Boolean selected) {
    this.selected = selected;
  }
}