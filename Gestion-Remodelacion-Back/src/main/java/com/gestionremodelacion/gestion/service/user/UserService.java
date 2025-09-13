package com.gestionremodelacion.gestion.service.user;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.dto.request.UserRequest;
import com.gestionremodelacion.gestion.dto.response.UserResponse;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.exception.DuplicateResourceException;
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.mapper.UserMapper;
import com.gestionremodelacion.gestion.model.Role;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.repository.RefreshTokenRepository;
import com.gestionremodelacion.gestion.repository.RoleRepository;
import com.gestionremodelacion.gestion.repository.UserRepository;

/**
 * Servicio para manejar la lógica de negocio relacionada con usuarios.
 */
@Service
public class UserService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            UserMapper userMapper, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable, String filter) {
        User currentUser = getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        String effectiveFilter = (filter != null && !filter.trim().isEmpty()) ? filter.trim().toLowerCase() : null;

        Page<User> usersPage = userRepository.findByEmpresaIdAndFilter(empresaId, effectiveFilter, pageable);
        return usersPage.map(userMapper::toDto);
    }

    /**
     * Obtiene el usuario autenticado actualmente desde el contexto de seguridad de
     * Spring.
     * Este es el método central para obtener información del usuario que realiza
     * una petición.
     *
     * @return La entidad User completa del usuario logueado.
     * @throws ResourceNotFoundException si no se encuentra el usuario en la BD.
     * @throws IllegalStateException     si no hay un usuario autenticado en el
     *                                   contexto.
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        // 1. Obtiene el objeto de autenticación del contexto de seguridad.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException(ErrorCatalog.NO_AUTHENTICATED_USER.getKey());
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCatalog.AUTHENTICATED_USER_NOT_FOUND.getKey()));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User currentUser = getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        return userRepository.findByIdAndEmpresaId(id, empresaId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.USER_NOT_FOUND.getKey()));

    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        User adminUser = getCurrentUser(); // El admin que crea al nuevo usuario

        // 1. Validar si el usuario ya existe
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateResourceException(ErrorCatalog.USERNAME_ALREADY_EXISTS.getKey());
        }

        User newUser = userMapper.toEntity(userRequest);
        // ASIGNACIÓN CLAVE: El nuevo usuario pertenece a la misma empresa que el admin
        // que lo creó.
        newUser.setEmpresa(adminUser.getEmpresa());

        newUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        newUser.setEnabled(userRequest.isEnabled());

        Set<Role> roles = userRequest.getRoles().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey())))
                .collect(Collectors.toSet());
        newUser.setRoles(roles);

        User savedUser = userRepository.save(newUser);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User currentUser = getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        // 1. Validar si el usuario existe
        User existingUser = userRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.USER_NOT_FOUND.getKey()));

        // 2. Validar si el nombre de usuario ya existe
        if (!existingUser.getUsername().equals(userRequest.getUsername()) &&
                userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateResourceException(
                    ErrorCatalog.USERNAME_ALREADY_EXISTS.getKey());
        }

        // 3. Actualizar el nombre y el estado
        existingUser.setUsername(userRequest.getUsername());
        existingUser.setEnabled(userRequest.isEnabled());

        // 4. Actualizar la clave solo si se proporciona
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        Set<Role> updatedRoles = new HashSet<>();
        // 5. Solo actualizar los roles si se proporcionan
        if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
            updatedRoles = userRequest.getRoles().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey())))
                    .collect(Collectors.toSet());
        }
        // 6. Asignar los nuevos roles al usuario
        existingUser.setRoles(updatedRoles);
        // 7. Guardar los cambios
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteById(Long id) {
        User currentUser = getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        User userToDelete = userRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.USER_NOT_FOUND.getKey()));

        if (currentUser.getId().equals(id)) {
            throw new BusinessRuleException(ErrorCatalog.CANNOT_DELETE_OWN_ACCOUNT.getKey());
        }

        refreshTokenRepository.deleteByUser(userToDelete);
        userRepository.delete(userToDelete);
    }

    @Transactional
    public UserResponse updateUserRoles(Long id, Set<Long> roleIds) {
        User currentUser = getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        User existingUser = userRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.USER_NOT_FOUND.getKey()));

        Set<Role> newRoles = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey())))
                .collect(Collectors.toSet());

        existingUser.setRoles(newRoles);
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    // Puedes mantener este método si es necesario para autenticación o casos
    // específicos
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

}
