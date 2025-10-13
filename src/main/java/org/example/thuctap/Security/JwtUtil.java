package org.example.thuctap.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
@Component
public class JwtUtil {

    // secret và expiration sẽ đọc từ application.properties (static helper)
    private static final String SECRET_BASE64 = System.getProperty("jwt.secret"); // fallback
    private static final long EXP_MS = Long.parseLong(System.getProperty("jwt.expiration-ms", "86400000"));

    // nếu bạn muốn đọc từ Spring Environment: use @Value in a @Component class.
    // For simplicity here, we'll fallback to a static secret if System property not set:
    private static final String DEFAULT_SECRET_BASE64 = "Z2VuZXJhdGVkLXlvdXItYmFzZTY0LXNlY3JldC1mb3ItdGVzdHMxMjM0NTY=";

    private static SecretKey getSigningKey() {
        String keyBase64 = System.getProperty("jwt.secret") != null ? System.getProperty("jwt.secret") : DEFAULT_SECRET_BASE64;
        byte[] keyBytes = Decoders.BASE64.decode(keyBase64);
        return Keys.hmacShaKeyFor(keyBytes); // throws if not long enough -> good
    }

    public static String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXP_MS);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static String extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException ex) {
            return null;
        }
    }

    public static boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}
