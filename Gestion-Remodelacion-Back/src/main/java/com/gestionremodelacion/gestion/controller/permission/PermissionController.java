package com.gestionremodelacion.gestion.controller.permission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gestionremodelacion.gestion.dto.request.PermissionRequest;
import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.dto.response.PermissionResponse;
import com.gestionremodelacion.gestion.service.permission.PermissionService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_READ')") // Nuevo permiso
    public ResponseEntity<ApiResponse<Page<PermissionResponse>>> getAllPermissions(
            @PageableDefault(size = 100, page = 0, sort = "id") Pageable pageable) {
        Page<PermissionResponse> permissions = permissionService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {
        PermissionResponse permission = permissionService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(permission));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')") // Nuevo permiso
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(@Valid @RequestBody PermissionRequest permissionDto) {
        PermissionResponse createdPermission = permissionService.createPermission(permissionDto);
        return ResponseEntity.ok(ApiResponse.success(createdPermission));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')") // Nuevo permiso
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionRequest permissionDto) {
        PermissionResponse updatedPermission = permissionService.updatePermission(id, permissionDto);
        return ResponseEntity.ok(ApiResponse.success(updatedPermission));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')") // Nuevo permiso
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
