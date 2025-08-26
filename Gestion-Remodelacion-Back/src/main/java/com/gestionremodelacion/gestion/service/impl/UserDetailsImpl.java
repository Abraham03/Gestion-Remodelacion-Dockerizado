package com.gestionremodelacion.gestion.service.impl;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gestionremodelacion.gestion.model.Permission;
import com.gestionremodelacion.gestion.model.User;

/**
 * Implementación personalizada de UserDetails para Spring Security.
 *
 * Usado por: - Spring Security para la autenticación y autorización -
 * JwtAuthFilter para construir el contexto de seguridad - AuthService para
 * generar tokens JWT
 *
 * Contiene: - Información básica del usuario (id, username, password) - Roles
 * convertidos a autoridades de Spring Security - Métodos para verificar estado
 * de la cuenta
 */
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Collection<? extends GrantedAuthority> userRoles; // 👈 SE AGREGA: Colección para solo los roles

    // Constructor principal
    public UserDetailsImpl(Long id, String username, String password,
            Collection<? extends GrantedAuthority> authorities, Collection<? extends GrantedAuthority> userRoles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.userRoles = userRoles;
    }

    /**
     * Método factory para construir UserDetailsImpl desde un User
     *
     * Área crítica: Conversión de roles a autoridades - Asegura que cada rol
     * tenga el prefijo "ROLE_" si no lo tiene
     */
    public static UserDetailsImpl build(User user) {
        Set<GrantedAuthority> authorities = new java.util.HashSet<>();
        Set<GrantedAuthority> userRoles = new java.util.HashSet<>();

        // 1. Recopilar roles y agregarlos a ambas colecciones
        user.getRoles().forEach(role -> {
            String roleName = role.getName();
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
            authorities.add(new SimpleGrantedAuthority(roleName));
            userRoles.add(new SimpleGrantedAuthority(roleName));
        });
        // 2. Recopilar permisos y agregarlos solo a la colección de authorities
        user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                userRoles
        );
    }

    // Getters y métodos requeridos por UserDetails
    public Long getId() {
        return id;
    }

    // ⭐️ SE AGREGA: Nuevo getter para los roles
    public Collection<? extends GrantedAuthority> getUserRoles() {
        return userRoles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
