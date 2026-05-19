package com.ebusiness.dto.order;

import java.math.BigDecimal;

public class OrderItemResponse {
  private Long id;
  private String productIdentifier;
  private Long productId;
  private String productName;
  private String productSubtitle;
  private String productCoverUrl;
  private BigDecimal productPrice;
  private Integer quantity;
  private BigDecimal subtotal;

  public OrderItemResponse(Long id, String productIdentifier, Long productId, String productName, String productSubtitle,
      String productCoverUrl, BigDecimal productPrice, Integer quantity, BigDecimal subtotal) {
    this.id = id;
    this.productIdentifier = productIdentifier;
    this.productId = productId;
    this.productName = productName;
    this.productSubtitle = productSubtitle;
    this.productCoverUrl = productCoverUrl;
    this.productPrice = productPrice;
    this.quantity = quantity;
    this.subtotal = subtotal;
  }

  public Long getId() {
    return id;
  }

  public String getProductIdentifier() {
    return productIdentifier;
  }

  public Long getProductId() {
    return productId;
  }

  public String getProductName() {
    return productName;
  }

  public String getProductSubtitle() {
    return productSubtitle;
  }

  public String getProductCoverUrl() {
    return productCoverUrl;
  }

  public BigDecimal getProductPrice() {
    return productPrice;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }
}