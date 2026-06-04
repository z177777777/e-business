package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.common.BusinessException;
import com.ebusiness.common.CurrentUserUtil;
import com.ebusiness.common.ErrorCode;
import com.ebusiness.entity.User;
import com.ebusiness.repository.UserRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
  private final UserRepository userRepository;

  public WalletController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> getBalance() {
    Long userId = CurrentUserUtil.getCurrentUserId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "user not found"));
    BigDecimal bal = user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO;
    Map<String, Object> result = new HashMap<>();
    result.put("balance", bal.doubleValue());
    return ApiResponse.success(result);
  }

  @PostMapping("/top-up")
  public ApiResponse<Map<String, Object>> topUp(@RequestParam double amount) {
    if (amount <= 0) {
      throw new BusinessException(ErrorCode.INVALID_PARAM, "充值金额必须大于0");
    }
    Long userId = CurrentUserUtil.getCurrentUserId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "user not found"));
    BigDecimal prev = user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO;
    BigDecimal newBal = prev.add(BigDecimal.valueOf(amount));
    user.setWalletBalance(newBal);
    userRepository.save(user);
    Map<String, Object> result = new HashMap<>();
    result.put("balance", newBal.doubleValue());
    return ApiResponse.success(result);
  }

  @PostMapping("/pay/{amount}")
  public ApiResponse<Map<String, Object>> pay(@PathVariable double amount) {
    if (amount <= 0) {
      throw new BusinessException(ErrorCode.INVALID_PARAM, "支付金额必须大于0");
    }
    Long userId = CurrentUserUtil.getCurrentUserId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "user not found"));
    BigDecimal prev = user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO;
    BigDecimal deduct = BigDecimal.valueOf(amount);
    if (prev.compareTo(deduct) < 0) {
      throw new BusinessException(ErrorCode.BIZ_ERROR, "余额不足");
    }
    BigDecimal newBal = prev.subtract(deduct);
    user.setWalletBalance(newBal);
    userRepository.save(user);
    Map<String, Object> result = new HashMap<>();
    result.put("balance", newBal.doubleValue());
    return ApiResponse.success(result);
  }
}
