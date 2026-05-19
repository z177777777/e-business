package com.ebusiness.config;

import com.ebusiness.entity.User;
import com.ebusiness.repository.UserRepository;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final StringRedisTemplate redisTemplate;
  private final UserRepository userRepository;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, StringRedisTemplate redisTemplate, UserRepository userRepository) {
    this.jwtUtil = jwtUtil;
    this.redisTemplate = redisTemplate;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        Claims claims = jwtUtil.parseToken(token);
        String jti = claims.getId();
        String key = "session:" + jti;
        String userIdStr = redisTemplate.opsForValue().get(key);
        if (userIdStr != null) {
          Long userId = Long.valueOf(userIdStr);
          Optional<User> userOptional = userRepository.findById(userId);
          if (userOptional.isPresent() && userOptional.get().getStatus() == 1) {
            User user = userOptional.get();
            UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail());
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
        }
      } catch (Exception ignored) {
        // Ignore invalid token
      }
    }
    filterChain.doFilter(request, response);
  }
}
