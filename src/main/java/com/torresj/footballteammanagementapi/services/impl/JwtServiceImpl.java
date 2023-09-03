package com.torresj.footballteammanagementapi.services.impl;

import com.torresj.footballteammanagementapi.services.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class JwtServiceImpl implements JwtService {

  @Value("${jwt.token.secret}")
  private final String secret;

  @Value("${jwt.token.expiration}")
  private final String expiration;

  @Value("${jwt.token.prefix}")
  private final String prefix;

  @Value("${jwt.token.issuer.info}")
  private final String issuer;

  @Override
  public String createJWS(String name) {
    log.debug("[JWT SERVICE] Generating JWT");
    return Jwts.builder()
        .setIssuedAt(new Date())
        .setIssuer(issuer)
        .setSubject(name)
        .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(expiration)))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  @Override
  public String validateJWS(String jws) {
    var claims =
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseClaimsJws(jws.replace(prefix, ""))
            .getBody();
    return claims.getSubject();
  }
}
