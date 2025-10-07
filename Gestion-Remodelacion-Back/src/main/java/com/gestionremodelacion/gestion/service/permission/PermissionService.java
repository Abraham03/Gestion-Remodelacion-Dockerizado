package com.gestionremodelacion.gestion.service.permission;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gestionremodelacion.gestion.dto.request.PermissionRequest;
import com.gestionremodelacion.gestion.dto.response.PermissionDropdownResponse;
import com.gestionremodelacion.gestion.dto.response.PermissionResponse;
import com.gestionremodelacion.gestion.mapper.PermissionMapper;
import com.gestionremodelacion.gestion.model.Permission;
import com.gestionremodelacion.gestion.model.Permission.PermissionScope;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.repository.PermissionRepository;
import com.gestionremodelacion.gestion.service.user.UserService;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final UserService userService;

    public PermissionService(PermissionRepository permissionRepository, PermissionMapper permissionMapper,
            UserService userService) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
        this.userService = userService;
    }

    // Metodo para obtener los permisos para PermissionDropdownResponse
    @Transactional(readOnly = true)
    public List<PermissionDropdownResponse> getPermissionsForDropdown() {
        User currentUser = userService.getCurrentUser();

        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));

        // ordenamos por defecto nombre ASC
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        if (isSuperAdmin) {
            return permissionRepository.findAll(sort).stream() // Se usa el método correcto findAll(sort)
                    .map(permission -> new PermissionDropdownResponse(permission.getId(), permission.getName(),
                            permission.getDescription()))
                    .collect(Collectors.toList());
        }

        // Mapea los permisos a PermissionDropdownResponse
        return permissionRepository.findByScope(PermissionScope.TENANT, sort).stream()
                .map(permission -> new PermissionDropdownResponse(permission.getId(), permission.getName(),
                        permission.getDescription()))
                .collect(Collectors.toList());

    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> findAll(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        // Verificar si el usuario actual tiene el rol de Super Admin
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getName()));

        // Si es Super Admin, puede ver y asignar todos los permisos
        if (isSuperAdmin) {
            return permissionRepository.findAll(pageable)
                    .map(permissionMapper::toPermissionResponse);
        }

        // Si NO es Super Admin, (es un admin de empresa) solo devolvemos los permisos
        // de scope 'TENANT'
        return permissionRepository.findByScope(PermissionScope.TENANT, pageable)
                .map(permissionMapper::toPermissionResponse);
    }

    @Transactional(readOnly = true)
    public PermissionResponse findById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found with id " + id));
        return permissionMapper.toPermissionResponse(permission);
    }

    @Transactional
    public PermissionResponse createPermission(PermissionRequest permissionRequest) { // Cambiado a permissionRequest
        if (permissionRepository.existsByName(permissionRequest.getName())) { // Comprobar si ya existe
            throw new IllegalArgumentException("Permission name '" + permissionRequest.getName() + "' already exists.");
        }
        Permission permission = permissionMapper.toEntity(permissionRequest); // Usar el mapper
        Permission savedPermission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(savedPermission);
    }

    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest permissionRequest) { // Cambiado a
                                                                                               // permissionRequest
        Permission existingPermission = permissionRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found with id " + id));

        // Evitar cambiar el nombre a uno que ya existe y no sea el propio
        if (!existingPermission.getName().equals(permissionRequest.getName())
                && permissionRepository.existsByName(permissionRequest.getName())) {
            throw new IllegalArgumentException("Permission name '" + permissionRequest.getName() + "' already exists.");
        }

        existingPermission.setName(permissionRequest.getName());
        existingPermission.setDescription(permissionRequest.getDescription());
        // Obtener el scope del request y establecerlo en la entidad
        existingPermission.setScope(PermissionScope.valueOf(permissionRequest.getScope()));
        Permission updatedPermission = permissionRepository.save(existingPermission);
        return permissionMapper.toPermissionResponse(updatedPermission);
    }

    @Transactional
    public void deletePermission(Long id) { // Retorna void, no ApiResponse<Void>
        // 1. Busca el permiso por su ID. Si no lo encuentra, arroja un error 404.
        // Esto es más eficiente que hacer un `existsById` y luego un `deleteById`.
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el permiso con id " + id));

        // 2. VERIFICACIÓN CLAVE: Revisa si la lista de roles asociados a este permiso
        // NO está vacía.
        if (permission.getRoles() != null && !permission.getRoles().isEmpty()) {
            // 3. Si hay roles usándolo, lanza un error 409 Conflict con un mensaje claro.
            // El frontend recibirá este mensaje y podrá mostrárselo al usuario.
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este permiso no se puede eliminar porque está asignado a uno o más roles.");
        }

        // 4. Si la verificación pasa (no hay roles asociados), elimina el permiso.
        permissionRepository.delete(permission);
    }

}
