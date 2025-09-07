package com.gestionremodelacion.gestion.service.impl;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gestionremodelacion.gestion.empresa.model.Empresa;
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
    private final Empresa empresa;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Collection<? extends GrantedAuthority> userRoles;

    // Constructor principal
    public UserDetailsImpl(Long id, String username, String password, Empresa empresa,
            Collection<? extends GrantedAuthority> authorities, Collection<? extends GrantedAuthority> userRoles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.empresa = empresa;
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

        // 1. Recopilar roles, agregar prefijo "ROLE_" y añadirlos a ambas colecciones
        user.getRoles().forEach(role -> {
            String roleName = role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName();
            SimpleGrantedAuthority roleAuthority = new SimpleGrantedAuthority(roleName);
            authorities.add(roleAuthority);
            userRoles.add(roleAuthority);
        });
        // 2. Recopilar permisos y agregarlos solo a la colección de authorities
        user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .forEach(authorities::add);

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmpresa(),
                authorities,
                userRoles);
    }

    // Getters y métodos requeridos por UserDetails
    public Long getId() {
        return id;
    }

    /**
     * Devuelve la colección COMPLETA de autoridades (roles + permisos).
     * Usado por Spring Security para las comprobaciones de @PreAuthorize.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Devuelve una colección que contiene ÚNICAMENTE los roles del usuario.
     * Usado para construir el claim 'roles' en el JWT.
     */
    public Collection<? extends GrantedAuthority> getUserRoles() {
        return userRoles;
    }

    /**
     * 
     * Devuelve una colección que contiene ÚNICAMENTE los permisos del usuario.
     * Lo hace filtrando la lista completa de authorities para excluir los roles.
     * Usado para construir el claim 'authorities' en el JWT.
     */
    public Collection<? extends GrantedAuthority> getUserPermissions() {
        return authorities.stream()
                .filter(authority -> !userRoles.contains(authority))
                .collect(Collectors.toSet());
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

    public Empresa getEmpresa() {
        return empresa;
    }
}
