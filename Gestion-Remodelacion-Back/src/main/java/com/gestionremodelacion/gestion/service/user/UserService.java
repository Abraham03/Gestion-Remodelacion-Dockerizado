package com.gestionremodelacion.gestion.service.user;

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
import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.empresa.repository.EmpresaRepository;
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
    private final EmpresaRepository empresaRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            UserMapper userMapper, RefreshTokenRepository refreshTokenRepository, EmpresaRepository empresaRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.refreshTokenRepository = refreshTokenRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable, String filter) {
        User currentUser = getCurrentUser();
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));

        String effectiveFilter = (filter != null && !filter.trim().isEmpty()) ? filter.trim().toLowerCase() : null;
        Page<User> usersPage;

        if (isSuperAdmin) {
            // El SUPER ADMIN ve a todos los usuarios de todas las empresas.
            usersPage = userRepository.findAllWithFilter(effectiveFilter, pageable);
        } else {
            // Un ADMIN normal solo ve los usuarios de su propia empresa.
            if (currentUser.getEmpresa() == null) {
                // Si un usuario no-super-admin no tiene empresa, no debería ver a nadie.
                return Page.empty(pageable);
            }
            Long empresaId = currentUser.getEmpresa().getId();
            usersPage = userRepository.findByEmpresaIdAndFilter(empresaId, effectiveFilter, pageable);
        }

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
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));

        if (isSuperAdmin) {
            // El SUPER ADMIN puede buscar cualquier usuario por ID.
            return userRepository.findById(id)
                    .map(userMapper::toDto)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.USER_NOT_FOUND.getKey()));
        } else {
            // El ADMIN normal solo puede buscar usuarios dentro de su empresa.
            Long empresaId = currentUser.getEmpresa().getId();
            return userRepository.findByIdAndEmpresaId(id, empresaId)
                    .map(userMapper::toDto)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.USER_NOT_FOUND.getKey()));
        }
    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        User currentUser = getCurrentUser(); // El admin que crea al nuevo usuario

        validateRoleAssignment(currentUser, userRequest.getRoles());

        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateResourceException(ErrorCatalog.USERNAME_ALREADY_EXISTS.getKey());
        }

        User newUser = userMapper.toEntity(userRequest);

        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));

        // --- LÓGICA CORREGIDA ---
        if (isSuperAdmin) {
            // Si es Super Admin, debe recibir el 'empresaId' en el request.
            if (userRequest.getEmpresaId() == null) {
                throw new BusinessRuleException(ErrorCatalog.COMPANY_ID_REQUIRED.getKey()); // Deberías crear este error
                                                                                            // en tu catálogo
            }
            Empresa empresaAsignada = empresaRepository.findById(userRequest.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.COMPANY_NOT_FOUND.getKey())); // Y
                                                                                                                // este
                                                                                                                // también
            newUser.setEmpresa(empresaAsignada);
        } else {
            // Si es un Admin normal, se le asigna su propia empresa.
            newUser.setEmpresa(currentUser.getEmpresa());
        }
        // --- FIN DE LA CORRECCIÓN ---

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
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));

        // 1. Validar que un ADMIN no pueda asignar el rol de SUPER_ADMIN
        // Esta validación es crucial para la seguridad.
        validateRoleAssignment(currentUser, userRequest.getRoles());

        // 2. Encontrar al usuario existente según el rol del editor
        User existingUser;
        if (isSuperAdmin) {
            // El SUPER ADMIN puede editar cualquier usuario, lo busca solo por su ID.
            existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.USER_NOT_FOUND.getKey()));
        } else {
            // Un ADMIN normal solo puede editar usuarios de su propia empresa.
            if (currentUser.getEmpresa() == null) {
                // Si por alguna razón un ADMIN no tiene empresa, no puede editar a nadie.
                throw new ResourceNotFoundException(ErrorCatalog.USER_NOT_FOUND.getKey());
            }
            Long empresaId = currentUser.getEmpresa().getId();
            existingUser = userRepository.findByIdAndEmpresaId(id, empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.USER_NOT_FOUND.getKey()));
        }

        // 3. Validar si el nuevo nombre de usuario ya está en uso por OTRO usuario
        if (!existingUser.getUsername().equals(userRequest.getUsername()) &&
                userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateResourceException(ErrorCatalog.USERNAME_ALREADY_EXISTS.getKey());
        }

        // 4. Actualizar los campos básicos del usuario
        existingUser.setUsername(userRequest.getUsername());
        existingUser.setEnabled(userRequest.isEnabled());

        // 5. Actualizar la contraseña solo si se proporciona una nueva
        if (userRequest.getPassword() != null && !userRequest.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        // 6. Actualizar los roles
        if (userRequest.getRoles() != null) {
            Set<Role> updatedRoles = userRequest.getRoles().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey())))
                    .collect(Collectors.toSet());
            existingUser.setRoles(updatedRoles);
        }

        // 7. (FUNCIONALIDAD EXTRA PARA SUPER ADMIN) Permitir cambiar al usuario de
        // empresa
        if (isSuperAdmin && userRequest.getEmpresaId() != null) {
            Empresa empresaAsignada = empresaRepository.findById(userRequest.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.COMPANY_NOT_FOUND.getKey()));
            existingUser.setEmpresa(empresaAsignada);
        }

        // 8. Guardar y devolver la respuesta
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

    // MÉTODO PRIVADO PARA CENTRALIZAR LA VALIDACIÓN
    private void validateRoleAssignment(User currentUser, Set<Long> requestedRoleIds) {
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));

        // Si el usuario que hace la petición NO es Super Admin...
        if (!isSuperAdmin) {
            // Buscamos si entre los roles solicitados está el de Super Admin
            Optional<Role> superAdminRole = roleRepository.findByName("ROLE_SUPER_ADMIN");

            if (superAdminRole.isPresent() && requestedRoleIds.contains(superAdminRole.get().getId())) {
                // Si está, lanzamos una excepción porque no tiene permiso para asignarlo.
                throw new BusinessRuleException(ErrorCatalog.SUPER_ADMIN_ROLE_ASSIGNMENT_NOT_ALLOWED.getKey());
            }
        }
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
