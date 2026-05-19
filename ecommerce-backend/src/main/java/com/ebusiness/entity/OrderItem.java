package com.ebusiness.entity;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "product_identifier", nullable = false, length = 150)
  private String productIdentifier;

  @Column(name = "product_name", nullable = false, length = 200)
  private String productName;

  @Column(name = "product_subtitle", length = 255)
  private String productSubtitle;

  @Column(name = "product_cover_url", length = 512)
  private String productCoverUrl;

  @Column(name = "product_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal productPrice;

  @Column(nullable = false)
  private Integer quantity;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getOrderId() { return orderId; }
  public void setOrderId(Long orderId) { this.orderId = orderId; }
  public Long getProductId() { return productId; }
  public void setProductId(Long productId) { this.productId = productId; }
  public String getProductIdentifier() { return productIdentifier; }
  public void setProductIdentifier(String productIdentifier) { this.productIdentifier = productIdentifier; }
  public String getProductName() { return productName; }
  public void setProductName(String productName) { this.productName = productName; }
  public String getProductSubtitle() { return productSubtitle; }
  public void setProductSubtitle(String productSubtitle) { this.productSubtitle = productSubtitle; }
  public String getProductCoverUrl() { return productCoverUrl; }
  public void setProductCoverUrl(String productCoverUrl) { this.productCoverUrl = productCoverUrl; }
  public BigDecimal getProductPrice() { return productPrice; }
  public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }
  public Integer getQuantity() { return quantity; }
  public void setQuantity(Integer quantity) { this.quantity = quantity; }
  public BigDecimal getSubtotal() { return subtotal; }
  public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
