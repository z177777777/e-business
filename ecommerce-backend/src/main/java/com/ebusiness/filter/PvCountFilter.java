package com.ebusiness.filter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PvCountFilter extends OncePerRequestFilter {
  private final StringRedisTemplate redisTemplate;
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public PvCountFilter(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      if (shouldCount(request)) {
        String key = "pv:" + LocalDate.now().format(DATE_FMT);
        Long v = redisTemplate.opsForValue().increment(key);
        if (v != null && v == 1L) {
          // set expire 3 days
          redisTemplate.expireAt(key, java.util.Date.from(java.time.ZonedDateTime.now().plusDays(3).toInstant()));
        }
      }
    } catch (Exception e) {
      // ignore redis errors
      logger.warn("pv count failed", e);
    }
    filterChain.doFilter(request, response);
  }

  private boolean shouldCount(HttpServletRequest request) {
    String method = request.getMethod();
    if (!"GET".equalsIgnoreCase(method)) return false;
    String path = request.getRequestURI();
    // exclude admin and auth and uploads and api endpoints
    if (path.startsWith("/api/admin") || path.startsWith("/api/auth") || path.startsWith("/uploads") || path.startsWith("/api/")) {
      // if it's a regular api call, skip counting
      return false;
    }
    // exclude static resource extensions
    String lower = path.toLowerCase();
    if (lower.endsWith(".js") || lower.endsWith(".css") || lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".svg") || lower.endsWith(".ico") || lower.endsWith(".map")) {
      return false;
    }
    return true;
  }
}
