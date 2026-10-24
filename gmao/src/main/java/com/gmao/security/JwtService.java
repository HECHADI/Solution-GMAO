package com.gmao.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Génération/validation des JWT.
 *
 * ⚠️ Nécessite la dépendance jjwt (io.jsonwebtoken:jjwt-api / jjwt-impl /
 * jjwt-jackson, version 0.12.x) dans le pom.xml — pas incluse par défaut
 * dans un Spring Boot standard.
 *
 * ⚠️ Configure une vraie valeur pour `jwt.secret` dans application.properties
 * (au moins 32 caractères aléatoires) avant d'aller en production — la
 * valeur par défaut ci-dessous n'est là que pour que l'app démarre en dev.
 */
@Service
public class JwtService {

    @Value("${jwt.secret:CHANGE_ME_dev_only_secret_key_min_32_chars_long_please}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}") // 24h par défaut
    private long expirationMs;

    public String genererToken(String email, Integer utilisateurId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("utilisateurId", utilisateurId);
        claims.put("role", role);
        return construireToken(claims, email);
    }

  private String construireToken(Map<String, Object> claims, String subject) {
    Date maintenant = new Date();
    Date expiration = new Date(maintenant.getTime() + expirationMs);

    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(maintenant)
            .setExpiration(expiration)
            .signWith(cle(), SignatureAlgorithm.HS256)
            .compact();
}

    public String extraireEmail(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    public Integer extraireUtilisateurId(String token) {
        return extraireToutesClaims(token).get("utilisateurId", Integer.class);
    }

    public String extraireRole(String token) {
        return extraireToutesClaims(token).get("role", String.class);
    }

    public boolean estValide(String token, UserDetails userDetails) {
        String email = extraireEmail(token);
        return email.equals(userDetails.getUsername()) && !estExpire(token);
    }

    private boolean estExpire(String token) {
        return extraireExpiration(token).before(new Date());
    }

    private Date extraireExpiration(String token) {
        return extraireClaim(token, Claims::getExpiration);
    }

    private <T> T extraireClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extraireToutesClaims(token);
        return resolver.apply(claims);
    }

   private Claims extraireToutesClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(cle())
            .build()
            .parseClaimsJws(token)
            .getBody();
}

    private SecretKey cle() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
