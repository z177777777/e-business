package com.ebusiness.dto;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AddCartItemRequest {
  @NotBlank(message = "productId is required")
  private String productId;

  @NotBlank(message = "productName is required")
  private String productName;

  private String productSubtitle;

  private String category;

  private String coverSnapshot;

  @NotNull(message = "unitPrice is required")
  private BigDecimal unitPrice;

  @Min(value = 1, message = "quantity must be at least 1")
  private Integer quantity = 1;

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public String getProductSubtitle() {
    return productSubtitle;
  }

  public void setProductSubtitle(String productSubtitle) {
    this.productSubtitle = productSubtitle;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getCoverSnapshot() {
    return coverSnapshot;
  }

  public void setCoverSnapshot(String coverSnapshot) {
    this.coverSnapshot = coverSnapshot;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(BigDecimal unitPrice) {
    this.unitPrice = unitPrice;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }
}