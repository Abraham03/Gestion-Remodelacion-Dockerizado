package com.gestionremodelacion.gestion.service.auth;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.dto.request.LoginRequest;
import com.gestionremodelacion.gestion.dto.request.RefreshTokenRequest;
import com.gestionremodelacion.gestion.dto.response.AuthResponse;
import com.gestionremodelacion.gestion.model.Permission;
import com.gestionremodelacion.gestion.model.RefreshToken;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.security.jwt.JwtUtils;
import com.gestionremodelacion.gestion.service.impl.UserDetailsImpl;

/**
 * Servicio de autenticación con: - Manejo de transacciones - Rotación de tokens
 * - Validación de credenciales - Inyección de dependencias
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            RefreshTokenService refreshTokenService,
            TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // OBTENER ROLES Y AUTORIDADES POR SEPARADO
            List<String> permissions = userDetails.getUserPermissions().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            List<String> roles = userDetails.getUserRoles().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Generar tokens
            String jwtToken = jwtUtils.generateJwtToken(authentication);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            return new AuthResponse(
                    jwtToken,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    permissions,
                    roles,
                    jwtUtils.getExpirationDateFromToken(jwtToken),
                    refreshToken.getToken(),
                    userDetails.getEmpresa().getId(),
                    userDetails.getEmpresa().getPlan().toString(),
                    userDetails.getEmpresa().getLogoUrl());

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        // 1. Validar refresh token
        RefreshToken refreshToken = refreshTokenService.rotateRefreshToken(request.getRefreshToken());

        // 2. Generar nuevo access token
        User user = refreshToken.getUser();

        // RECOPILAR PERMISOS Y ROLES POR SEPARADO (LÓGICA CLAVE)
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct() // Asegura que no haya permisos duplicados
                .collect(Collectors.toList());

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName())
                .collect(Collectors.toList());

        String newJwtToken = jwtUtils.generateTokenFromUsername(
                user.getUsername(),
                permissions,
                roles,
                user.getEmpresa());

        return new AuthResponse(
                newJwtToken,
                user.getId(),
                user.getUsername(),
                permissions,
                roles,
                jwtUtils.getExpirationDateFromToken(newJwtToken),
                refreshToken.getToken(),
                user.getEmpresa().getId(),
                user.getEmpresa().getPlan().toString(),
                user.getEmpresa().getLogoUrl());
    }

    @Transactional
    public void logout(String token) {
        try {
            // Date expirationDate = jwtUtils.getExpirationDateFromToken(token);
            Date expirationDate = jwtUtils.getExpirationDateFromExpiredToken(token); // 👈 Asumimos que existe un método
                                                                                     // similar

            tokenBlacklistService.blacklistToken(token, expirationDate.toInstant());
            refreshTokenService.revokeByToken(token);
        } catch (Exception e) {
            throw new RuntimeException("Error al cerrar sesión", e);
        }
    }

}
