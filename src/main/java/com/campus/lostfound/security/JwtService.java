package com.campus.lostfound.security;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service managing JWT generation, cryptographic signing (HMAC-SHA256), and claims validation.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:1800000}")
    private long jwtExpirationMs;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.trim().length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters (256 bits) long for secure HMAC-SHA256 signing.");
        }
        if ("CampusLostAndFoundSecureJwtSecretKeyForDevelopmentOnly2026!".equals(jwtSecret.trim())) {
            log.warn("SECURITY WARNING: Using default development JWT secret. Set JWT_SECRET environment variable for production environments!");
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT token containing essential user identity and role claims.
     *
     * @param user authenticated user entity
     * @return signed compact JWT string
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String subject = user.getCollegeUid() != null ? user.getCollegeUid() : user.getEmail();
        String roleStr = user.getRole() != null ? user.getRole().name() : Role.STUDENT.name();

        return Jwts.builder()
                .subject(subject)
                .claim("userId", user.getId())
                .claim("collegeUid", user.getCollegeUid())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .claim("role", roleStr)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates a JWT token's signature, structure, and expiration.
     *
     * @param token bearer token
     * @return true if valid and unexpired; false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("JWT token is expired: {}", ex.getMessage());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Extracts all claims from a verified JWT token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the subject (College UID or Email) from the token.
     */
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the user primary key ID from the token claims.
     */
    public Long extractUserId(String token) {
        Object val = extractAllClaims(token).get("userId");
        if (val instanceof Number num) {
            return num.longValue();
        }
        return null;
    }

    /**
     * Extracts the user role from the token claims.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}

