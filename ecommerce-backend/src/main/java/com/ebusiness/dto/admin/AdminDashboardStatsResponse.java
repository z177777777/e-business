package com.ebusiness.dto.admin;

public class AdminDashboardStatsResponse {
  private final long userCount;
  private final long productCount;
  private final long orderCount;
  private final long pendingOrderCount;
  private final long pendingPasswordResetRequestCount;
  private final long pvToday;

  public AdminDashboardStatsResponse(long userCount, long productCount, long orderCount, long pendingOrderCount, long pendingPasswordResetRequestCount, long pvToday) {
    this.userCount = userCount;
    this.productCount = productCount;
    this.orderCount = orderCount;
    this.pendingOrderCount = pendingOrderCount;
    this.pendingPasswordResetRequestCount = pendingPasswordResetRequestCount;
    this.pvToday = pvToday;
  }

  public long getUserCount() {
    return userCount;
  }

  public long getProductCount() {
    return productCount;
  }

  public long getOrderCount() {
    return orderCount;
  }

  public long getPendingOrderCount() {
    return pendingOrderCount;
  }

  public long getPendingPasswordResetRequestCount() {
    return pendingPasswordResetRequestCount;
  }

  public long getPvToday() {
    return pvToday;
  }
}