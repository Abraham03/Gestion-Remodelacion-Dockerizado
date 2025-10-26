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
import com.gestionremodelacion.gestion.dto.response.RoleDropdownResponse;
import com.gestionremodelacion.gestion.dto.response.RoleResponse;
import com.gestionremodelacion.gestion.empresa.model.Empresa;
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

    // Método de ayuda para verificar si el usuario es Super Admin
    private boolean isSuperAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> findAll(Pageable pageable, String searchTerm) {
        User currentUser = userService.getCurrentUser();
        Page<Role> rolesPage;
        String effectiveSearchTerm = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm : null;

        // Validar si el usuario es Super Admin
        if (isSuperAdmin(currentUser)) {
            // El Super Admin ve todos los roles
            rolesPage = (searchTerm != null && !searchTerm.trim().isEmpty())
                    ? roleRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTerm,
                            searchTerm, pageable)
                    : roleRepository.findAll(pageable);
        } else {
            // Valida si el usuario tiene una empresa asignada
            if (currentUser.getEmpresa() == null) {
                // No debería ver a nadie
                return Page.empty();
            }
            // Obtener el ID de la empresa
            Long empresaId = currentUser.getEmpresa().getId();
            // Solo puede ver los roles de su empresa
            rolesPage = roleRepository.findByEmpresaIdAndFilter(empresaId, effectiveSearchTerm, pageable);
        }

        return rolesPage.map(roleMapper::toRoleResponse);
    }

    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        User currentUser = userService.getCurrentUser();
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey()));

        // Un ADMIN normal solo puede ver roles que pertenezcan a su propia empresa.
        if (!isSuperAdmin(currentUser)) {
            // Si el rol es global (empresa null) o de otra empresa, se niega el acceso.
            if (role.getEmpresa() == null || !role.getEmpresa().getId().equals(currentUser.getEmpresa().getId())) {
                throw new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey()); // Se simula que no existe.
            }
        }
        return roleMapper.toRoleResponse(role);
    }

    @Transactional
    public RoleResponse createRole(RoleRequest roleRequest) {
        User currentUser = userService.getCurrentUser();
        Empresa empresaDelUsuario = currentUser.getEmpresa();

        // Un ADMIN debe tener una empresa para poder crear un rol.
        if (!isSuperAdmin(currentUser) && empresaDelUsuario == null) {
            throw new BusinessRuleException("error.company.requiredForAdmins");
        }

        // Se verifica que el nombre del rol no exista ya DENTRO de la misma empresa.
        Long empresaIdParaBusqueda = isSuperAdmin(currentUser) ? null : empresaDelUsuario.getId();
        if (roleRepository.existsByNameAndEmpresaId(roleRequest.getName(), empresaIdParaBusqueda)) {
            throw new DuplicateResourceException(ErrorCatalog.ROLE_NAME_ALREADY_EXISTS.getKey());
        }

        // Validar que los permisos sean de la misma empresa
        validatePermissionsScope(roleRequest.getPermissions());
        Role role = roleMapper.toRole(roleRequest);

        // El rol pertenece a la empresa del ADMIN que lo crea. Si es SUPER_ADMIN, es un
        // rol global (empresa null).
        if (!isSuperAdmin(currentUser)) {
            role.setEmpresa(empresaDelUsuario);
        }

        Set<Permission> permissions = roleRequest.getPermissions().stream()
                .map(permissionId -> permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.PERMISSION_NOT_FOUND.getKey())))
                .collect(Collectors.toSet());
        role.setPermissions(permissions);

        Role savedRole = roleRepository.save(role);
        return roleMapper.toRoleResponse(savedRole);
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest roleRequest) {
        User currentUser = userService.getCurrentUser();
        Empresa empresaDelUsuario = currentUser.getEmpresa();

        // Buscamos el rol a actualizar
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey()));

        // --- VALIDACIONES DE SEGURIDAD ---
        if (!isSuperAdmin(currentUser)) {
            // Un ADMIN solo puede editar roles de su propia empresa.
            if (existingRole.getEmpresa() == null
                    || !existingRole.getEmpresa().getId().equals(empresaDelUsuario.getId())) {
                throw new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey()); // Se simula que no existe
            }
            // Un ADMIN no puede editar el rol ADMIN base (si existiera y fuera global)
            if ("ROLE_ADMIN".equals(existingRole.getName()) && existingRole.getEmpresa() == null) {
                throw new BusinessRuleException("error.role.cannotEditBaseAdmin");
            }
        }

        // Se verifica si el nombre del rol ha cambiado Y si el nuevo ya existe en la
        // misma empresa
        if (!existingRole.getName().equals(roleRequest.getName())
                && roleRepository.existsByNameAndEmpresaId(roleRequest.getName(), empresaDelUsuario.getId())) {
            throw new DuplicateResourceException(ErrorCatalog.ROLE_NAME_ALREADY_EXISTS.getKey());
        }

        // Validar que los permisos sean de la misma empresa
        validatePermissionsScope(roleRequest.getPermissions());

        // Si no hay conflicto, actualizamos
        roleMapper.updateRoleFromRequest(roleRequest, existingRole); // Usar el método del mapper para actualizar campos

        // Actualizamos los permisos
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

    // MÉTODO PARA POBLAR LOS DROPDOWNS EN LOS FORMULARIOS DEL SUPERADMIN
    @Transactional(readOnly = true)
    public List<RoleDropdownResponse> findAllForDropdown(Long empresaId) {
        User currentUser = userService.getCurrentUser();
        List<Role> roles;

        if (isSuperAdmin(currentUser)) {
            // Si es Super Admin Y se proporciono un ID de empresa, filtra por esa empresa
            if (empresaId != null) {
                roles = roleRepository.findAllByEmpresaId(empresaId);
            } else {
                roles = roleRepository.findAll();
            }
        } else {
            // Si no es Super Admin, solo puede ver los roles de su propia empresa
            if (currentUser.getEmpresa() == null)
                return List.of();
            Long userEmpresaId = currentUser.getEmpresa().getId();
            roles = roleRepository.findAllByEmpresaId(userEmpresaId);
        }

        // Se asegura de que Super Admin nunca se envie a un ADMIN normal
        if (!isSuperAdmin(currentUser)) {
            roles = roles.stream()
                    .filter(role -> !"ROLE_SUPER_ADMIN".equals(role.getName()))
                    .collect(Collectors.toList());
        }

        return roles.stream()
                .map(role -> new RoleDropdownResponse(role.getId(), role.getName()))
                .collect(Collectors.toList());

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
            userRepository.save(user);
        }

        // Finalmente, elimina el rol.
        roleRepository.deleteById(id);
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
        User currentUser = userService.getCurrentUser();

        if (isSuperAdmin(currentUser)) {
            // Un SUPER_ADMIN puede buscar roles globales (sin empresa asignada).
            return roleRepository.findByNameAndEmpresaIsNull(name);
        } else {
            // Un ADMIN normal busca roles por nombre DENTRO de su empresa.
            if (currentUser.getEmpresa() == null) {
                return Optional.empty();
            }
            Long empresaId = currentUser.getEmpresa().getId();
            return roleRepository.findByNameAndEmpresaId(name, empresaId);
        }
    }

}
