package org.example.thuctap.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretBase64;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        String token = Jwts.builder()
                .setSubject(username)
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        System.out.println("==> [JwtUtil] Token generated for user: " + username);
        return token;
    }

    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("==> [JwtUtil] Token claims: " + claims);
            return claims.getSubject();
        } catch (JwtException ex) {
            System.out.println("==> [JwtUtil] Lỗi khi parse token: " + ex.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            System.out.println("==> [JwtUtil] Token hợp lệ!");
            return true;
        } catch (JwtException ex) {
            System.out.println("==> [JwtUtil] Token không hợp lệ: " + ex.getMessage());
            return false;
        }
    }
}
