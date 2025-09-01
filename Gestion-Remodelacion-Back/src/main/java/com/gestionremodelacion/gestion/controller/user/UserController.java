package com.gestionremodelacion.gestion.controller.user;

import java.util.Optional;
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
import org.springframework.web.bind.annotation.RestController; // Asegúrate de tener esta clase o una similar

import com.gestionremodelacion.gestion.dto.request.UserRequest; // Importa Optional
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
        return ResponseEntity.ok(ApiResponse.success(usersPage));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')") // Modificado: Requiere el permiso 'USER_CREATE'
    public ResponseEntity<ApiResponse<?>> createUser(@Valid @RequestBody UserRequest userRequest) {
        Optional<UserResponse> createdUser = userService.createUser(userRequest);
        if (createdUser.isPresent()) {
            ApiResponse<UserResponse> successResponse = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Usuario creado con éxito",
                    createdUser.get());
            return ResponseEntity.ok(successResponse);

        } else {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    HttpStatus.CONFLICT.value(),
                    "El nombre de usuario ya existe.",
                    null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);

        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {
        Optional<UserResponse> updatedUser = userService.updateUser(id, userRequest);
        if (updatedUser.isPresent()) {
            ApiResponse<UserResponse> successResponse = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Usuario actualizado con éxito",
                    updatedUser.get());
            return ResponseEntity.ok(successResponse);
        } else {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    "El nombre ya está en uso por otro usuario.",
                    null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')") // Modificado: Requiere el permiso 'USER_DELETE'
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // Si tienes este método, asegúrate de que el body sea un Set<String>
    // y no un UserRequest completo si lo usas con un método
    // userService.updateRolesForUser
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_UPDATE_ROLES')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(@PathVariable Long id,
            @RequestBody Set<String> roleNames) {
        // Asumiendo que userService.updateRolesForUser(id, roleNames) existe
        UserResponse updatedUser = userService.updateUserRoles(id, roleNames);
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }

}
