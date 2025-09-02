package com.gestionremodelacion.gestion.service.user;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.dto.request.UserRequest;
import com.gestionremodelacion.gestion.dto.response.UserResponse;
import com.gestionremodelacion.gestion.exception.DuplicateResourceException;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.mapper.UserMapper;
import com.gestionremodelacion.gestion.model.Role;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.repository.RefreshTokenRepository;
import com.gestionremodelacion.gestion.repository.RoleRepository;
import com.gestionremodelacion.gestion.repository.UserRepository;

/**
 * Servicio para manejar la lógica de negocio relacionada con usuarios.
 */
@Service
public class UserService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            UserMapper userMapper, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable, String filter) {
        Page<User> usersPage = (filter != null && !filter.trim().isEmpty()
                ? userRepository.findByUsernameContainingIgnoreCase(filter, pageable)
                : userRepository.findAll(pageable));
        return usersPage.map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        // 1. Validar si el usuario ya existe
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateResourceException("El nombre de usuario '" + userRequest.getUsername() + "' ya existe.");
        }
        User user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setEnabled(userRequest.isEnabled()); // Set enabled status

        Set<Role> roles = userRequest.getRoles().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId)))
                .collect(Collectors.toSet());

        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        // 2. Verificamos si el nombre de usuario ha cambiado Y si el nuevo ya existe
        if (!existingUser.getUsername().equals(userRequest.getUsername())
                && userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateResourceException(
                    "El nombre de usuario '" + userRequest.getUsername() + "' ya está en uso.");
        }
        // 3. Si no hay conflicto, actualizamos los datos
        existingUser.setUsername(userRequest.getUsername());
        existingUser.setEnabled(userRequest.isEnabled());

        // 4. Solo actualizar el password solo si se proporciona
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        // 5. Actualizar los roles basado en los roleIds proporcionados
        Set<Role> updatedRoles = new HashSet<>();
        if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
            updatedRoles = userRequest.getRoles().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId)))
                    .collect(Collectors.toSet());
        }

        // 6. Asignar los nuevos roles al usuario
        existingUser.setRoles(updatedRoles);
        // 7. Guardar el usuario actualizado
        User updatedUser = userRepository.save(existingUser);
        // 8. Devolver el DTO del usuario actualizado
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteById(Long id) {
        // 1. Verificar si el usuario existe para lanzar una excepción si no es así.
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        // 2. Eliminar todos los tokens de actualización asociados a este usuario.
        // Esto previene el error de clave foránea.
        refreshTokenRepository.deleteByUser(user); // Asumo que tienes este método en tu RefreshTokenRepository

        // 3. Ahora que las dependencias han sido eliminadas, puedes eliminar al
        // usuario.
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse updateUserRoles(Long id, Set<String> roleNames) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        Set<Role> newRoles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName) // Assuming findByName exists in RoleRepository
                        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con nombre: " + roleName)))
                .collect(Collectors.toSet());

        existingUser.setRoles(newRoles);
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    // Puedes mantener este método si es necesario para autenticación o casos
    // específicos
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

}
