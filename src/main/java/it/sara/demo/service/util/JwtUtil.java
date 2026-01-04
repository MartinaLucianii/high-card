package it.sara.demo.service.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Generate JWT token
    public String generateToken(String username) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(jwtIssuer) // <-- ISSUER
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationMs)) // <-- EXPIRATION
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Get username from JWT token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validate JWT token (signature + exp + issuer)
    public boolean validateJwtToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody();

            // issuer validation
            return jwtIssuer != null && jwtIssuer.equals(claims.getIssuer());

        } catch (SecurityException e) {
            System.out.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
}