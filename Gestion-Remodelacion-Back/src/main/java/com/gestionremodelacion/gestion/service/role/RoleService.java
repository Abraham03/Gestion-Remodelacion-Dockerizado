package com.gestionremodelacion.gestion.service.role;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gestionremodelacion.gestion.dto.request.RoleRequest;
import com.gestionremodelacion.gestion.dto.response.RoleResponse;
import com.gestionremodelacion.gestion.mapper.RoleMapper;
import com.gestionremodelacion.gestion.model.Permission;
import com.gestionremodelacion.gestion.model.Role;
import com.gestionremodelacion.gestion.repository.PermissionRepository;
import com.gestionremodelacion.gestion.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.roleMapper = roleMapper;
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> findAll(Pageable pageable, String searchTerm) {
        Page<Role> rolesPage;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            // Correctly pass searchTerm to both 'name' and 'description' parameters
            rolesPage = roleRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTerm, searchTerm, pageable);
        } else {
            rolesPage = roleRepository.findAll(pageable);
        }
        return rolesPage.map(roleMapper::toRoleResponse);
    }

    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with id " + id));
        return roleMapper.toRoleResponse(role); // Usar el método del mapper
    }

    @Transactional
    public RoleResponse createRole(RoleRequest roleRequest) {
        if (roleRepository.existsByName(roleRequest.getName())) {
            throw new IllegalArgumentException("Role name '" + roleRequest.getName() + "' already exists.");
        }
        Role role = roleMapper.toRole(roleRequest); // Usar el método del mapper

        Set<Permission> permissions = new HashSet<>();
        if (roleRequest.getPermissions() != null && !roleRequest.getPermissions().isEmpty()) {
            permissions = roleRequest.getPermissions().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: Permission '" + permissionId + "' not found.")))
                    .collect(Collectors.toSet());
        }
        role.setPermissions(permissions); // Asignar los permisos después del mapeo

        Role savedRole = roleRepository.save(role);
        return roleMapper.toRoleResponse(savedRole); // Usar el método del mapper
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest roleRequest) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with id " + id));

        if (!existingRole.getName().equals(roleRequest.getName()) && roleRepository.existsByName(roleRequest.getName())) {
            throw new IllegalArgumentException("Role name '" + roleRequest.getName() + "' already exists.");
        }

        roleMapper.updateRoleFromRequest(roleRequest, existingRole); // Usar el método del mapper para actualizar campos básicos

        Set<Permission> newPermissions = new HashSet<>();
        if (roleRequest.getPermissions() != null && !roleRequest.getPermissions().isEmpty()) {
            // La sintaxis correcta para un stream que transforma IDs en entidades de Permiso
            newPermissions = roleRequest.getPermissions().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: Permission '" + permissionId + "' not found.")))
                    .collect(Collectors.toSet());
        }
        existingRole.setPermissions(newPermissions); // Reemplaza los permisos existentes

        Role updatedRole = roleRepository.save(existingRole);
        return roleMapper.toRoleResponse(updatedRole); // Usar el método del mapper
    }

    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with id " + id);
        }
        // Considera implementar lógica para desvincular el rol de los usuarios antes de eliminarlo
        roleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

}
