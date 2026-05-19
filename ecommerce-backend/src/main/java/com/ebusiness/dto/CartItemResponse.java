package com.ebusiness.dto;

import java.math.BigDecimal;

public class CartItemResponse {
  private Long id;
  private String productId;
  private String productName;
  private String productSubtitle;
  private String category;
  private String coverSnapshot;
  private BigDecimal unitPrice;
  private Integer quantity;
  private Boolean selected;
  private BigDecimal subtotal;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public Boolean getSelected() {
    return selected;
  }

  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
  }
}