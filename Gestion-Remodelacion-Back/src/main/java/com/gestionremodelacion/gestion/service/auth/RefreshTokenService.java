package com.gestionremodelacion.gestion.service.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.model.RefreshToken;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.repository.RefreshTokenRepository;
import com.gestionremodelacion.gestion.repository.UserRepository;
import com.gestionremodelacion.gestion.security.exception.TokenRefreshException;

/**
 * Servicio para manejo de refresh tokens con: - Rotación de tokens - Revocación
 * de tokens - Verificación de expiración - Manejo de concurrencia
 */
@Service
public class RefreshTokenService {

    private static final String USER_NOT_FOUND = "Usuario no encontrado con ID: ";
    private static final String TOKEN_REVOKED_LOG = "Token revocado: {}";
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND + userId));

        // Primero marcamos todos como usados
        refreshTokenRepository.markAllAsUsedByUserId(userId);

        // Luego eliminamos los existentes
        refreshTokenRepository.deleteAllByUserId(userId);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setUsed(false);

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        logger.info("Nuevo refresh token creado para usuario: {}", userId);

        return savedToken;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String oldRefreshToken) {
        // 1. Validar token antiguo
        RefreshToken oldToken = verifyExpiration(findByToken(oldRefreshToken));

        // Revocar el token antiguo primero
        revokeByToken(oldToken.getToken());

        // 3. Eliminar físicamente el token viejo
        refreshTokenRepository.delete(oldToken);

        // 4. Crear nuevo token
        return createRefreshToken(oldToken.getUser().getId());
    }

    // --- Nuevo método para verificar expiración próxima ---
    public boolean isExpiringSoon(String token, Duration threshold) {
        RefreshToken refreshToken = findByToken(token);
        Instant now = Instant.now();
        return refreshToken.getExpiryDate().minus(threshold).isBefore(now);
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Refresh token no encontrado"));
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            logger.warn(TOKEN_REVOKED_LOG, token.getToken());
            throw new TokenRefreshException(token.getToken(), "Token expirado");
        }

        if (token.isUsed()) {
            refreshTokenRepository.delete(token);
            logger.warn(TOKEN_REVOKED_LOG, token.getToken());
            throw new TokenRefreshException(token.getToken(), "Token ya utilizado");
        }

        return token;
    }

    public Optional<RefreshToken> findByUser(String username) {
        return refreshTokenRepository.findByUser_Username(username);
    }

    @Transactional
    public void revokeByToken(String token) {
        // Usamos el método optimizado del repositorio
        int updated = refreshTokenRepository.markAsUsed(token);
        if (updated > 0) {
            logger.info("Token revocado: {}", token);
        }
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        // Primero marcamos todos como usados
        int marked = refreshTokenRepository.markAllAsUsedByUserId(userId);

        // Luego eliminamos
        refreshTokenRepository.deleteAllByUserId(userId);

        logger.info("Revocados {} refresh tokens para usuario ID: {}", marked, userId);
    }

    public boolean isTokenRevoked(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(RefreshToken::isUsed)
                .orElse(true); // Si no existe, se considera revocado
    }

    public boolean isRefreshTokenValid(Long userId) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUserId(userId);
        return refreshToken.isPresent()
                && !refreshToken.get().isUsed()
                && refreshToken.get().getExpiryDate().isAfter(Instant.now());
    }
}
