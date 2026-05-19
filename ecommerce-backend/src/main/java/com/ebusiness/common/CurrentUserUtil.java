package com.ebusiness.common;

import com.ebusiness.config.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUserUtil {
  private CurrentUserUtil() {}

  public static Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserPrincipal)) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    return ((UserPrincipal) principal).getId();
  }
}
