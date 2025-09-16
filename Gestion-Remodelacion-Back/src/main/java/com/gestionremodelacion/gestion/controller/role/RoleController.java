package com.gestionremodelacion.gestion.controller.role;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Roles obtenidos con éxito", rolesResponse));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('USER_CREATE') or hasAuthority('USER_UPDATE')") // Permiso para crear/editar usuarios
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRolesForForm() {
        List<RoleResponse> roles = roleService.findAllForForm();
        return ResponseEntity
                .ok(new ApiResponse<>(HttpStatus.OK.value(), "Roles para formulario obtenidos con éxito", roles));
    }

    @GetMapping("/{id}") // NEW: Get Role by ID
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse role = roleService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Role obtenido con éxito", role));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest role) {
        RoleResponse createdRole = roleService.createRole(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Role creado con éxito", createdRole));

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(@PathVariable Long id,
            @Valid @RequestBody RoleRequest role) {
        RoleResponse updatedRole = roleService.updateRole(id, role);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Role actualizado con éxito", updatedRole));

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Role eliminado con éxito", null));
    }

}
