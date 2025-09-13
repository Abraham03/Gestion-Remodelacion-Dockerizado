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
import com.gestionremodelacion.gestion.exception.DuplicateResourceException;
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.mapper.RoleMapper;
import com.gestionremodelacion.gestion.model.Permission;
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
        Page<Role> rolesPage = (searchTerm != null && !searchTerm.trim().isEmpty())
                ? roleRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTerm, searchTerm,
                        pageable)
                : roleRepository.findAll(pageable);
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
        Long empresaId = currentUser.getEmpresa().getId();

        // 1. Verificar si el rol existe
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey()));

        // Desvincular el rol de los usuarios antes de eliminar
        List<User> usersWithRole = userRepository.findByRolesContainingAndEmpresaId(role, empresaId);
        for (User user : usersWithRole) {
            user.removeRole(role);
            userRepository.save(user);
        }
        // Eliminar Rol
        roleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

}
