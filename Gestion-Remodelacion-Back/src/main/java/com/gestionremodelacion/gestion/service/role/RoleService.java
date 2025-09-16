package com.gestionremodelacion.gestion.service.role;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.dto.request.RoleRequest;
import com.gestionremodelacion.gestion.dto.response.RoleResponse;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.exception.DuplicateResourceException;
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.mapper.RoleMapper;
import com.gestionremodelacion.gestion.model.Permission;
import com.gestionremodelacion.gestion.model.Permission.PermissionScope;
import com.gestionremodelacion.gestion.model.Role;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.repository.PermissionRepository;
import com.gestionremodelacion.gestion.repository.RoleRepository;
import com.gestionremodelacion.gestion.repository.UserRepository;
import com.gestionremodelacion.gestion.service.user.UserService;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final UserRepository userRepository;
    private final UserService userService;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, RoleMapper roleMapper,
            UserRepository userRepository, UserService userService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.roleMapper = roleMapper;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> findAll(Pageable pageable, String searchTerm) {
        User currentUser = userService.getCurrentUser();
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));

        Page<Role> rolesPage;

        // ✅ LÓGICA MODIFICADA
        if (isSuperAdmin) {
            // El Super Admin ve todos los roles
            rolesPage = (searchTerm != null && !searchTerm.trim().isEmpty())
                    ? roleRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTerm,
                            searchTerm, pageable)
                    : roleRepository.findAll(pageable);
        } else {
            // Un Admin normal solo ve los roles de "inquilino" (tenant)
            String effectiveSearchTerm = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm : null;
            rolesPage = roleRepository.findTenantRoles(effectiveSearchTerm, pageable);
        }

        return rolesPage.map(roleMapper::toRoleResponse);
    }

    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return roleRepository.findById(id)
                .map(roleMapper::toRoleResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey()));
    }

    @Transactional
    public RoleResponse createRole(RoleRequest roleRequest) {
        if (roleRepository.existsByName(roleRequest.getName())) {
            throw new DuplicateResourceException(ErrorCatalog.ROLE_NAME_ALREADY_EXISTS.getKey());
        }

        // Validar que los permisos sean de la misma empresa
        validatePermissionsScope(roleRequest.getPermissions());

        Role role = roleMapper.toRole(roleRequest); // Usar el método del mapper

        Set<Permission> permissions = new HashSet<>();
        if (roleRequest.getPermissions() != null && !roleRequest.getPermissions().isEmpty()) {
            permissions = roleRequest.getPermissions().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    ErrorCatalog.PERMISSION_NOT_FOUND.getKey())))
                    .collect(Collectors.toSet());
        }
        role.setPermissions(permissions); // Asignar los permisos después del mapeo

        Role savedRole = roleRepository.save(role);
        return roleMapper.toRoleResponse(savedRole);
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest roleRequest) {
        // 1. Buscamos el rol a actualizar
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey()));

        // 2. Verificamos si el nombre del rol ha cambiado Y si el nuevo ya existe
        if (!existingRole.getName().equals(roleRequest.getName())
                && roleRepository.existsByName(roleRequest.getName())) {
            throw new DuplicateResourceException(ErrorCatalog.ROLE_NAME_ALREADY_EXISTS.getKey());
        }

        // Validar que los permisos sean de la misma empresa
        validatePermissionsScope(roleRequest.getPermissions());

        // 3. Si no hay conflicto, actualizamos
        roleMapper.updateRoleFromRequest(roleRequest, existingRole); // Usar el método del mapper para actualizar campos

        // 4. Actualizamos los permisos
        Set<Permission> newPermissions = new HashSet<>();
        if (roleRequest.getPermissions() != null && !roleRequest.getPermissions().isEmpty()) {
            newPermissions = roleRequest.getPermissions().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    ErrorCatalog.PERMISSION_NOT_FOUND.getKey())))
                    .collect(Collectors.toSet());
        }
        // Reemplaza los permisos existentes
        existingRole.setPermissions(newPermissions);
        // Guardamos
        Role updatedRole = roleRepository.save(existingRole);
        return roleMapper.toRoleResponse(updatedRole);
    }

    @Transactional
    public void deleteRole(Long id) {
        User currentUser = userService.getCurrentUser();
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));

        Role roleToDelete = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey()));

        // Regla de negocio: No se puede eliminar el rol SUPER_ADMIN
        if ("ROLE_SUPER_ADMIN".equals(roleToDelete.getName())) {
            throw new BusinessRuleException("error.role.cannotDeleteSuperAdmin"); // Añadir esta clave a ErrorCatalog
        }

        List<User> usersWithRole;

        if (isSuperAdmin) {
            // El SUPER_ADMIN busca en TODOS los usuarios que tengan este rol.
            usersWithRole = userRepository.findByRolesContaining(roleToDelete);
        } else {
            // El ADMIN normal busca solo en los usuarios de SU empresa.
            // Se añade una validación para el caso (improbable) de que un admin no tenga
            // empresa.
            if (currentUser.getEmpresa() == null) {
                throw new BusinessRuleException("error.company.requiredForAdmins"); // Añadir esta clave
            }
            Long empresaId = currentUser.getEmpresa().getId();
            usersWithRole = userRepository.findByRolesContainingAndEmpresaId(roleToDelete, empresaId);
        }

        // Desvincula el rol de todos los usuarios encontrados.
        for (User user : usersWithRole) {
            user.removeRole(roleToDelete);
            userRepository.save(user); // Guarda cada usuario modificado.
        }

        // Finalmente, elimina el rol.
        roleRepository.deleteById(id);
    }

    // ✅ NUEVO MÉTODO PARA POBLAR LOS DROPDOWNS EN LOS FORMULARIOS
    @Transactional(readOnly = true)
    public List<RoleResponse> findAllForForm() {
        User currentUser = userService.getCurrentUser();
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));

        List<Role> roles;
        if (isSuperAdmin) {
            roles = roleRepository.findAll();
        } else {
            roles = roleRepository.findAllTenantRolesForForm();
        }

        return roles.stream()
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * MÉTODO DE SEGURIDAD
     * Verifica que un administrador de empresa no intente asignarse a sí mismo
     * o a otros un permiso de nivel de PLATAFORMA.
     */
    private void validatePermissionsScope(Set<Long> permissionIds) {
        User currentUser = userService.getCurrentUser();
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));

        // Si el usuario NO es Super Admin, no puede asignar permisos de PLATFORM.
        if (!isSuperAdmin) {
            List<Permission> requestedPermissions = permissionRepository.findAllById(permissionIds);
            boolean hasPlatformPermission = requestedPermissions.stream()
                    .anyMatch(p -> p.getScope() == PermissionScope.PLATFORM);

            if (hasPlatformPermission) {
                // Lanza un error si se intenta asignar un permiso no permitido.
                throw new BusinessRuleException(ErrorCatalog.PERMISSION_NOT_ALLOWED.getKey());
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

}
