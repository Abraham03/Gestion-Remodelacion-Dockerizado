package com.gestionremodelacion.gestion.controller.role;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestionremodelacion.gestion.dto.request.RoleRequest;
import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.dto.response.RoleResponse;
import com.gestionremodelacion.gestion.service.role.RoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<Page<RoleResponse>>> getAllRoles(
            @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable,
            @RequestParam(required = false) String searchTerm) {

        Page<RoleResponse> rolesResponse = roleService.findAll(pageable, searchTerm);
        return ResponseEntity.ok(ApiResponse.success(rolesResponse));
    }

    @GetMapping("/{id}") // NEW: Get Role by ID
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse role = roleService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ApiResponse<?>> createRole(@Valid @RequestBody RoleRequest role) {
        Optional<RoleResponse> createdRole = roleService.createRole(role);
        if (createdRole.isPresent()) {
            ApiResponse<RoleResponse> successResponse = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Role creado successfully",
                    createdRole.get());
            return ResponseEntity.ok(successResponse);
        } else {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    "El nombre del rol ya existe",
                    null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<?>> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest role) {
        Optional<RoleResponse> updatedRole = roleService.updateRole(id, role);
        if (updatedRole.isPresent()) {
            ApiResponse<RoleResponse> successResponse = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Role actualizado successfully",
                    updatedRole.get());
            return ResponseEntity.ok(successResponse);
        } else {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    "El nombre del rol ya existe",
                    null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

    }

    @DeleteMapping("/{id}") // NEW: Delete Role by ID
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
