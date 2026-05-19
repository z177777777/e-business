package com.ebusiness.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "cart_items")
public class CartItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

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

  @Column(nullable = false)
  private Boolean selected;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
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
  public Boolean getSelected() { return selected; }
  public void setSelected(Boolean selected) { this.selected = selected; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
}
