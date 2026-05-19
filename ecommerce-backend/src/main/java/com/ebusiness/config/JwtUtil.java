package com.ebusiness.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${app.jwt.secret}")
  private String secret;

  public TokenResult generateToken(Long userId, String email, long expireMinutes) {
    String jti = UUID.randomUUID().toString();
    Date now = new Date();
    Date exp = new Date(now.getTime() + expireMinutes * 60 * 1000);

    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    String token = Jwts.builder()
        .setId(jti)
        .setSubject(String.valueOf(userId))
        .claim("email", email)
        .setIssuedAt(now)
        .setExpiration(exp)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    return new TokenResult(token, exp.getTime(), jti);
  }

  public Claims parseToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }

  public static class TokenResult {
    private final String token;
    private final long expireAt;
    private final String jti;

    public TokenResult(String token, long expireAt, String jti) {
      this.token = token;
      this.expireAt = expireAt;
      this.jti = jti;
    }

    public String getToken() {
      return token;
    }

    public long getExpireAt() {
      return expireAt;
    }

    public String getJti() {
      return jti;
    }
  }
}
