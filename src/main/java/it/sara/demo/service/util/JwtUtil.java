package it.sara.demo.service.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility component to generate and validate JSON Web Tokens (JWT) using JJWT.
 *
 * <p>Validations enforced:
 * <ul>
 *   <li><b>Signature</b>: token must be signed with the configured secret</li>
 *   <li><b>Expiration</b>: token must not be expired (checked by JJWT during parsing)</li>
 *   <li><b>Issuer</b>: token issuer must match {@code jwt.issuer}</li>
 * </ul>
 *
 * <p>Properties required:
 * <ul>
 *   <li>{@code jwt.secret}: HMAC secret (must be long enough for HS256)</li>
 *   <li>{@code jwt.expiration}: token validity duration in milliseconds</li>
 *   <li>{@code jwt.issuer}: expected issuer string</li>
 * </ul>
 */
@Component
public class JwtUtil {

    /** HMAC secret used to sign and verify tokens. */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /** Token validity duration in milliseconds. */
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    /** Expected issuer to validate tokens against. */
    @Value("${jwt.issuer}")
    private String jwtIssuer;

    /** Cached signing key derived from {@link #jwtSecret}. */
    private SecretKey key;

    /**
     * Initializes the signing key after Spring injects configuration properties.
     * This avoids recreating the key on each request.
     *
     * @throws IllegalArgumentException if the secret is too short for HS256
     */
    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret must be provided");
        }
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT token for the given username (subject).
     *
     * <p>The token includes:
     * <ul>
     *   <li>{@code sub} = username</li>
     *   <li>{@code iss} = configured issuer</li>
     *   <li>{@code iat} = issued-at</li>
     *   <li>{@code exp} = expiration date</li>
     * </ul>
     *
     * @param username subject to store inside the JWT (e.g., email)
     * @return signed JWT token string
     */
    public String generateToken(String username) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the subject (username/email) from a valid token.
     *
     * @param token JWT token
     * @return subject contained in the token
     * @throws JwtException if token is invalid/expired/tampered
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validates a JWT token.
     *
     * <p>Validation steps:
     * <ul>
     *   <li>Parsing validates signature and expiration</li>
     *   <li>Issuer is checked explicitly against {@link #jwtIssuer}</li>
     * </ul>
     *
     * @param token JWT token
     * @return true if token is valid, not expired, and issuer matches; false otherwise
     */
    public boolean validateJwtToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody();

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