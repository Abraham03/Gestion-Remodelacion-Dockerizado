package com.gestionremodelacion.gestion.service.permission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gestionremodelacion.gestion.dto.request.PermissionRequest;
import com.gestionremodelacion.gestion.dto.response.PermissionResponse;
import com.gestionremodelacion.gestion.mapper.PermissionMapper;
import com.gestionremodelacion.gestion.model.Permission;
import com.gestionremodelacion.gestion.repository.PermissionRepository;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionService(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> findAll(Pageable pageable) {
        return permissionRepository.findAll(pageable)
                .map(permissionMapper::toPermissionResponse);
    }

    @Transactional(readOnly = true)
    public PermissionResponse findById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found with id " + id));
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
    public PermissionResponse updatePermission(Long id, PermissionRequest permissionRequest) { // Cambiado a permissionRequest
        Permission existingPermission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found with id " + id));

        // Evitar cambiar el nombre a uno que ya existe y no sea el propio
        if (!existingPermission.getName().equals(permissionRequest.getName()) && permissionRepository.existsByName(permissionRequest.getName())) {
            throw new IllegalArgumentException("Permission name '" + permissionRequest.getName() + "' already exists.");
        }

        existingPermission.setName(permissionRequest.getName());
        existingPermission.setDescription(permissionRequest.getDescription());
        Permission updatedPermission = permissionRepository.save(existingPermission);
        return permissionMapper.toPermissionResponse(updatedPermission);
    }

    @Transactional
    public void deletePermission(Long id) { // Retorna void, no ApiResponse<Void>
        if (!permissionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found with id " + id);
        }
        permissionRepository.deleteById(id);
        // No se devuelve ApiResponse.success(null) aquí, se hace en el controlador.
    }

}
