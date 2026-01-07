package com.ahmedyassin.TravelSmart.Security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secretKeyString) {
        SecretKey key;
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
            // La clé doit être d'au moins 256 bits (32 octets) pour HS256
            if (keyBytes.length < 32) {
                System.err.println("Provided JWT secret is too short (< 256 bits). Generating a secure key.");
                key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Génère une clé sécurisée si celle fournie est trop courte
            } else {
                key = Keys.hmacShaKeyFor(keyBytes);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Base64-encoded JWT secret: " + e.getMessage() + ". Generating a secure key.");
            key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Génère une clé sécurisée si l'encodage Base64 est invalide
        }
        this.secretKey = key;
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS))) // Token valide 1 jour
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            // Log l'exception pour le débogage (ex: token expiré, signature invalide)
            System.err.println("Error extracting username from token: " + e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            System.err.println("Error validating token: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            // Si le token est invalide ou corrompu, il sera considéré comme expiré ou non valide
            System.err.println("Error checking token expiration: " + e.getMessage());
            return true; // Considérer comme expiré ou invalide en cas d'erreur
        }
    }
}