package com.portfolio.urlshortener.security;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.time.Instant; import java.util.Date;
@Service public class JwtService {
 private final SecretKey key; private final long ttl;
 public JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-seconds:3600}") long ttl){if(secret.getBytes(StandardCharsets.UTF_8).length<32)throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.ttl=ttl;}
 public String generate(String subject){Instant now=Instant.now();return Jwts.builder().subject(subject).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(ttl))).signWith(key).compact();}
 public String subject(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();} public long getTtl(){return ttl;}
}
