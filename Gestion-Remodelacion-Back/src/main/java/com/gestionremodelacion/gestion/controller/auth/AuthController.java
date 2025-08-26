package com.gestionremodelacion.gestion.controller.auth;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestionremodelacion.gestion.dto.request.LoginRequest;
import com.gestionremodelacion.gestion.dto.request.RefreshTokenRequest;
import com.gestionremodelacion.gestion.dto.request.UserRequest;
import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.dto.response.AuthResponse;
import com.gestionremodelacion.gestion.model.Role;
import com.gestionremodelacion.gestion.security.jwt.JwtUtils;
import com.gestionremodelacion.gestion.service.auth.AuthService;
import com.gestionremodelacion.gestion.service.auth.RefreshTokenService;
import com.gestionremodelacion.gestion.service.role.RoleService;
import com.gestionremodelacion.gestion.service.user.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

/**
 * Controlador de autenticación con: - Documentación Swagger - Validación de
 * entrada - Manejo centralizado de errores - Respuestas estandarizadas
 */
@Tag(name = "Authentication", description = "API para manejo de autenticación")
@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final UserService userService;
    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final RoleService roleService;

    public AuthController(UserService userService, AuthService authService, JwtUtils jwtUtils , RefreshTokenService refreshTokenService, RoleService roleService) {
        this.userService = userService;
        this.authService = authService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.roleService = roleService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticate(loginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> registerUser(
            @Valid @RequestBody UserRequest signUpRequest) {

        // Asignar ROLE_USER por defecto si no se especifica
        if (signUpRequest.getRoles() == null || signUpRequest.getRoles().isEmpty()) { // Use getRoleIds
            // Fetch the 'USER' role by name and get its ID
            Role userRole = roleService.findByName("ROLE_USER") // Assuming role service has findByName
                    .orElseThrow(() -> new EntityNotFoundException("Default role 'ROLE_USER' not found. Please create it."));
            signUpRequest.setRoles(Set.of(userRole.getId())); // Set the ID as a Set<Long>
        }

        // Ensure 'enabled' is set for new users during signup if not already
        // Your UserRequest now has `enabled`. If your frontend doesn't send it for signup,
        // you might want a default here. Assuming `UserRequest` already has a default or is handled.
        if (Boolean.FALSE.equals(signUpRequest.isEnabled())) {
            signUpRequest.setEnabled(true);
        }
        
        userService.createUser(signUpRequest);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(201, "Usuario registrado exitosamente", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Eliminar "Bearer "
        authService.logout(token);

        // Obtener el refresh token del usuario actual y revocarlo también        
        String username = jwtUtils.getUserNameFromJwtToken(token);
        refreshTokenService.findByUser(username).ifPresent(refreshToken -> {
            refreshTokenService.revokeByToken(refreshToken.getToken());
        });

        return ResponseEntity.ok(
                new ApiResponse<>(200, "Sesión cerrada exitosamente", null));
    }
}
