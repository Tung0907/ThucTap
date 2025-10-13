package org.example.thuctap.Security;

import io.jsonwebtoken.*;
import java.util.Date;

public class JwtUtil {
    // Lưu ý: Đặt key ở application.properties hoặc biến môi trường trong production
    private static final String SECRET_KEY = "mySecretKey1234567890"; // đổi cho an toàn
    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 24; // 24 giờ

    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static String extractUsername(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    public static boolean isTokenValid(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
