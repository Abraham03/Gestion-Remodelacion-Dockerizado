package com.gestionremodelacion.gestion.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.gestionremodelacion.gestion.config.JwtProperties;
import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.service.impl.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;

/**
 * Utilidades JWT mejoradas con: - Métodos para claims personalizados -
 * Validación de token robusta - Manejo de expiración - Generación de tokens con
 * metadata
 */
@Component
public class JwtUtils {

    private final JwtProperties jwtProperties;
    private Key key;
    private final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    @PostConstruct
    public void init() {
        String secret = jwtProperties.getSecretKey();
        if (!StringUtils.hasText(secret)) {
            // Este error es más descriptivo y te dirá exactamente qué falló.
            throw new IllegalArgumentException("La clave secreta de JWT no puede ser nula o vacía. " +
                    "Verifica la propiedad 'jwt.secret' y la conexión con Secret Manager.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        // 1. Extraer solo los PERMISOS usando el nuevo método.
        List<String> permissions = userPrincipal.getUserPermissions().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 2. Extraer solo los ROLES.
        List<String> roles = userPrincipal.getUserRoles().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Empresa empresa = userPrincipal.getEmpresa();
        // Verifica si la empresa existe antes de obtener sus datos
        Long empresaId = (empresa != null) ? empresa.getId() : null;
        String plan = (empresa != null) ? empresa.getPlan().toString() : null;
        String nombreEmpresa = (empresa != null) ? empresa.getNombreEmpresa() : null;

        return buildToken(
                userPrincipal.getUsername(),
                permissions,
                roles,
                empresaId,
                plan,
                nombreEmpresa);
    }

    public String generateTokenFromUsername(String username, List<String> permissions, List<String> roles,
            Empresa empresa) {
        // Hacemos la misma verificación aquí
        Long empresaId = (empresa != null) ? empresa.getId() : null;
        String plan = (empresa != null) ? empresa.getPlan().toString() : null;
        String nombreEmpresa = (empresa != null) ? empresa.getNombreEmpresa() : null;
        return buildToken(username, permissions, roles, empresaId, plan, nombreEmpresa);
    }

    private String buildToken(String subject, List<String> permissions, List<String> roles, Long empresaId,
            String plan, String nombreEmpresa) {
        var builder = Jwts.builder()
                .setSubject(subject)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .claim("authorities", permissions)
                .claim("roles", roles);

        // Añadimos los claims de la empresa solo si no son nulos
        if (empresaId != null) {
            builder.claim("empresaId", empresaId);
        }
        if (plan != null) {
            builder.claim("plan", plan);
        }
        if (nombreEmpresa != null) {
            builder.claim("nombreEmpresa", nombreEmpresa);
        }

        return builder.signWith(key).compact();
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
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | SignatureException
                | IllegalArgumentException e) {
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

    // Método para obtener la fecha de expiración de un token caducado
    public Date getExpirationDateFromExpiredToken(String token) {
        try {
            // Intenta parsear el token, si falla con ExpiredJwtException, decodifica el
            // cuerpo
            return extractClaim(token, Claims::getExpiration);
        } catch (ExpiredJwtException e) {
            // Si el token ha expirado, podemos obtener la información directamente de la
            // excepción
            return e.getClaims().getExpiration();
        } catch (Exception e) {
            // Para cualquier otra excepción (ej. malformed token), puedes manejarlo como
            // desees
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
