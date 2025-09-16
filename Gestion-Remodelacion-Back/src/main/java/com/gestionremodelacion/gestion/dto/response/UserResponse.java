package com.gestionremodelacion.gestion.dto.response;

import java.util.Collections;
import java.util.Set;

public class UserResponse {

    private Long id;
    private String username;
    private boolean enabled;
    private Set<RoleResponse> roles; // Changed to Set<RoleResponse>
    private Long empresaId;
    private String nombreEmpresa;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public UserResponse(Long id, String username, boolean enabled, Set<RoleResponse> roles, Long empresaId,
            String nombreEmpresa) {
        this.id = id;
        this.username = username;
        this.enabled = enabled;
        this.roles = roles != null ? roles : Collections.emptySet();
        this.empresaId = empresaId;
        this.nombreEmpresa = nombreEmpresa;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<RoleResponse> getRoles() {
        return roles != null ? roles : Collections.emptySet();
    }

    public void setRoles(Set<RoleResponse> roles) {
        this.roles = roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

}
