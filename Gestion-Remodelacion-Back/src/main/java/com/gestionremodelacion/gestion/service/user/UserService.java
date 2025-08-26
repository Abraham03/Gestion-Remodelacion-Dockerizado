package com.gestionremodelacion.gestion.service.user;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gestionremodelacion.gestion.dto.request.UserRequest;
import com.gestionremodelacion.gestion.dto.response.UserResponse;
import com.gestionremodelacion.gestion.mapper.UserMapper;
import com.gestionremodelacion.gestion.model.Role;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.repository.RefreshTokenRepository;
import com.gestionremodelacion.gestion.repository.RoleRepository;
import com.gestionremodelacion.gestion.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Servicio para manejar la lógica de negocio relacionada con usuarios.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable, String searchTerm) {
        log.info("Fetching all users with searchTerm: {}", searchTerm); // New log
        Page<User> usersPage;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            usersPage = userRepository.findByUsernameContainingIgnoreCase(searchTerm, pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }
        log.debug("Found {} users on page {}.", usersPage.getTotalElements(), pageable.getPageNumber()); // New log

        return usersPage.map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        User user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword())); // Encode password
        user.setEnabled(userRequest.isEnabled()); // Set enabled status

        Set<Role> roles = userRequest.getRoles().stream()
                .map(roleId -> roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId)))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        existingUser.setUsername(userRequest.getUsername());
        existingUser.setEnabled(userRequest.isEnabled()); // Update enabled status

        // Only update password if provided in the request
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        // Update roles based on provided roleIds
        Set<Role> updatedRoles = new HashSet<>();
        if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
            updatedRoles = userRequest.getRoles().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId)))
                    .collect(Collectors.toSet());
        }
        existingUser.setRoles(updatedRoles);

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteById(Long id) {
        // 1. Verificar si el usuario existe para lanzar una excepción si no es así.
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id " + id));

        // 2. Eliminar todos los tokens de actualización asociados a este usuario.
        // Esto previene el error de clave foránea.
        refreshTokenRepository.deleteByUser(user); // Asumo que tienes este método en tu RefreshTokenRepository

        // 3. Ahora que las dependencias han sido eliminadas, puedes eliminar al usuario.
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse updateUserRoles(Long id, Set<String> roleNames) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        Set<Role> newRoles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName) // Assuming findByName exists in RoleRepository
                .orElseThrow(() -> new EntityNotFoundException("Role not found with name: " + roleName)))
                .collect(Collectors.toSet());

        existingUser.setRoles(newRoles);
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    // Puedes mantener este método si es necesario para autenticación o casos específicos
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

}
