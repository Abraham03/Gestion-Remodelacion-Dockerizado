package com.gestionremodelacion.gestion.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.gestionremodelacion.gestion.config.JwtProperties;
import com.gestionremodelacion.gestion.service.impl.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

/**
 * Utilidades JWT mejoradas con: - Métodos para claims personalizados -
 * Validación de token robusta - Manejo de expiración - Generación de tokens con
 * metadata
 */
@Component
public class JwtUtils {

    private final JwtProperties jwtProperties;
    private final Key key;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return buildToken(
                userPrincipal.getUsername(),
                userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );
    }

    public String generateTokenFromUsername(String username, List<String> roles) {
        return buildToken(username, roles);
    }

    private String buildToken(String subject, List<String> roles) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .signWith(key)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | SignatureException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ Nuevo método para obtener la fecha de expiración de un token caducado
    public Date getExpirationDateFromExpiredToken(String token) {
        try {
            // Intenta parsear el token, si falla con ExpiredJwtException, decodifica el cuerpo
            return extractClaim(token, Claims::getExpiration);
        } catch (ExpiredJwtException e) {
            // Si el token ha expirado, podemos obtener la información directamente de la excepción
            return e.getClaims().getExpiration();
        } catch (Exception e) {
            // Para cualquier otra excepción (ej. malformed token), puedes manejarlo como desees
            throw new RuntimeException("Error al extraer la fecha de expiración del token", e);
        }
    }

    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpiringSoon(String token, int minutes) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.getTime() - System.currentTimeMillis() < (minutes * 60 * 1000);
    }

    public long getExpirationTimeFromToken(String token) {
        return extractClaim(token, Claims::getExpiration).getTime();
    }

    // Método para extraer todos los claims de forma segura
    public Optional<Claims> safeExtractAllClaims(String token) {
        try {
            return Optional.of(extractAllClaims(token));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
