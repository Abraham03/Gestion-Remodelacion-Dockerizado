package com.gestionremodelacion.gestion.controller.user;

import java.util.Set;

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

import com.gestionremodelacion.gestion.dto.request.UserRequest;
import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.dto.response.UserResponse;
import com.gestionremodelacion.gestion.service.user.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            @RequestParam(required = false) String filter) {
        Page<UserResponse> usersPage = userService.findAll(pageable, filter);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Usuarios obtenidos con éxito", usersPage));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Usuario obtenido con éxito", user));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')") // Modificado: Requiere el permiso 'USER_CREATE'
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse createdUser = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Usuario creado con éxito", createdUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        return ResponseEntity
                .ok(new ApiResponse<>(HttpStatus.OK.value(), "Usuario actualizado con éxito", updatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')") // Modificado: Requiere el permiso 'USER_DELETE'
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Usuario eliminado con éxito", null));
    }

    // Actualizar los roles de un usuario
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_UPDATE_ROLES')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(@PathVariable Long id,
            @RequestBody Set<Long> roleId) {
        // Asumiendo que userService.updateRolesForUser(id, roleNames) existe
        UserResponse updatedUser = userService.updateUserRoles(id, roleId);
        return ResponseEntity
                .ok(new ApiResponse<>(HttpStatus.OK.value(), "Roles de usuario actualizados con éxito", updatedUser));
    }

}
