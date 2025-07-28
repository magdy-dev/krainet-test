package com.krainet.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.issuer}")
    private String jwtIssuer;

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        log.debug("Generating JWT token for user: {}", userPrincipal.getUsername());
        
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        claims.put("roles", roles);
        log.trace("Adding roles to JWT token: {}", roles);
        
        String token = buildToken(claims, userPrincipal.getUsername());
        log.debug("Successfully generated JWT token for user: {}", userPrincipal.getUsername());
        
        return token;
    }

    public String generateToken(UserDetails userDetails) {
        log.debug("Generating JWT token for user: {}", userDetails.getUsername());
        
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
                
        claims.put("roles", roles);
        log.trace("Adding roles to JWT token: {}", roles);
        
        String token = buildToken(claims, userDetails.getUsername());
        log.debug("Successfully generated JWT token for user: {}", userDetails.getUsername());
        
        return token;
    }

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        log.trace("Building JWT token for subject: {}", subject);
        Date issuedAt = new Date();
        Date expiration = new Date(System.currentTimeMillis() + jwtExpirationMs);
        
        log.debug("Token details - Issued at: {}, Expires at: {}, Issuer: {}", 
                issuedAt, expiration, jwtIssuer);
                
        String token = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuer(jwtIssuer)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
                
        log.trace("Successfully built JWT token");
        return token;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.trace("Validating JWT token for user: {}", userDetails.getUsername());
        final String username = extractUsername(token);
        boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        
        if (!isValid) {
            log.warn("JWT token validation failed for user: {}", userDetails.getUsername());
            if (!username.equals(userDetails.getUsername())) {
                log.warn("Token username '{}' doesn't match user details username '{}'", 
                        username, userDetails.getUsername());
            }
            if (isTokenExpired(token)) {
                log.warn("Token is expired");
            }
        } else {
            log.debug("JWT token is valid for user: {}", userDetails.getUsername());
        }
        
        return isValid;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateJwtToken(String authToken) {
        log.trace("Validating JWT token");
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(authToken)
                .getBody();
                
            log.debug("Successfully validated JWT token for subject: {}, issued at: {}, expires at: {}", 
                    claims.getSubject(), claims.getIssuedAt(), claims.getExpiration());
                    
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            log.debug("Expired token details - Subject: {}, Expiration: {}", 
                    e.getClaims().getSubject(), e.getClaims().getExpiration());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage(), e);
        }
        return false;
    }
}
