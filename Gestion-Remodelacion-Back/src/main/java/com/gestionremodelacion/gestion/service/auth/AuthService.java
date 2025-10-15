package com.gestionremodelacion.gestion.service.auth;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.dto.request.LoginRequest;
import com.gestionremodelacion.gestion.dto.request.RefreshTokenRequest;
import com.gestionremodelacion.gestion.dto.request.UserRequest;
import com.gestionremodelacion.gestion.dto.response.AuthResponse;
import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.exception.DuplicateResourceException;
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.invitation.model.Invitacion;
import com.gestionremodelacion.gestion.invitation.repository.InvitacionRepository;
import com.gestionremodelacion.gestion.model.Permission;
import com.gestionremodelacion.gestion.model.RefreshToken;
import com.gestionremodelacion.gestion.model.Role;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.repository.RoleRepository;
import com.gestionremodelacion.gestion.repository.UserRepository;
import com.gestionremodelacion.gestion.security.jwt.JwtUtils;
import com.gestionremodelacion.gestion.service.impl.UserDetailsImpl;

import jakarta.persistence.EntityNotFoundException;

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
    // PasswordEncoder para los nuevos usuarios invitados
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final InvitacionRepository invitacionRepository;
    private final RoleRepository roleRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
            RefreshTokenService refreshTokenService,
            TokenBlacklistService tokenBlacklistService, InvitacionRepository invitacionRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userRepository = userRepository;
        this.invitacionRepository = invitacionRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
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

            // --- Obtener los datos de la empresa ----
            Empresa empresa = userDetails.getEmpresa();
            Long empresaId = (empresa != null) ? empresa.getId() : null;
            String plan = (empresa != null) ? empresa.getPlan().toString() : null;
            String logoUrl = (empresa != null) ? empresa.getLogoUrl() : null;
            String nombreEmpresa = (empresa != null) ? empresa.getNombreEmpresa() : null; // ✅ OBTENER EL NOMBRE

            // --- FIN DE LA CORRECCIÓN ---

            return new AuthResponse(
                    jwtToken,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    permissions,
                    roles,
                    jwtUtils.getExpirationDateFromToken(jwtToken),
                    refreshToken.getToken(),
                    empresaId,
                    plan,
                    logoUrl,
                    nombreEmpresa);

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

        // --- INICIO DE LA CORRECCIÓN ---
        Empresa empresa = user.getEmpresa();
        Long empresaId = (empresa != null) ? empresa.getId() : null;
        String plan = (empresa != null) ? empresa.getPlan().toString() : null;
        String logoUrl = (empresa != null) ? empresa.getLogoUrl() : null;
        String nombreEmpresa = (empresa != null) ? empresa.getNombreEmpresa() : null; // ✅ OBTENER EL NOMBRE

        // --- FIN DE LA CORRECCIÓN ---

        String newJwtToken = jwtUtils.generateTokenFromUsername(
                user.getUsername(),
                permissions,
                roles,
                empresa);

        return new AuthResponse(
                newJwtToken,
                user.getId(),
                user.getUsername(),
                permissions,
                roles,
                jwtUtils.getExpirationDateFromToken(newJwtToken),
                refreshToken.getToken(),
                empresaId,
                plan,
                logoUrl,
                nombreEmpresa);
    }

    // En: com.gestionremodelacion.gestion.service.auth.AuthService.java

    @Transactional
    public void registerUserFromInvitation(UserRequest request, String token) {
        // 1. Validar el token de invitación (esto ya estaba correcto)
        Invitacion invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(() -> new BusinessRuleException("El token de invitación no es válido o no existe."));

        if (invitacion.isUtilizada()) {
            throw new BusinessRuleException("Esta invitación ya ha sido utilizada.");
        }
        if (invitacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Esta invitación ha expirado.");
        }

        // 2. Verificar que el username no esté ya en uso
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(ErrorCatalog.USERNAME_ALREADY_EXISTS.getKey());
        }

        // 3. Obtener el ID de la empresa directamente desde la invitación.
        Empresa empresa = invitacion.getEmpresa();

        // Se obtiene el role a asignar
        String roleNameToAssign = invitacion.getRolAAsignar();

        // Busca el rol por nombre DENTRO de la empresa O a nivel global
        Role roleToAssign = roleRepository
                .findByNameAndEmpresaId(roleNameToAssign, empresa.getId())
                .or(() -> roleRepository.findByNameAndEmpresaIsNull(roleNameToAssign))
                .orElseThrow(() -> new EntityNotFoundException("El rol '" + roleNameToAssign + "' no fue encontrado."));

        // 5. Crear la nueva entidad de Usuario
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(invitacion.getEmail()); // Se toma el email de la invitación (más seguro)
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEnabled(true);
        newUser.setEmpresa(empresa);
        newUser.setRoles(Set.of(roleToAssign));

        // 6. Guardar el nuevo usuario
        userRepository.save(newUser);

        // 7. Marcar la invitación como utilizada
        invitacion.setUtilizada(true);
        invitacionRepository.save(invitacion);
    }

    @Transactional
    public void logout(String token) {
        try {
            // Date expirationDate = jwtUtils.getExpirationDateFromToken(token);
            Date expirationDate = jwtUtils.getExpirationDateFromExpiredToken(token);

            tokenBlacklistService.blacklistToken(token, expirationDate.toInstant());
            refreshTokenService.revokeByToken(token);
        } catch (Exception e) {
            throw new RuntimeException("Error al cerrar sesión", e);
        }
    }

}
