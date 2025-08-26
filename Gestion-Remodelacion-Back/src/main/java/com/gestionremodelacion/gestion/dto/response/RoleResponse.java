package com.gestionremodelacion.gestion.dto.response;

import java.util.Set;

public class RoleResponse {

    private Long id;
    private String name;
    private String description;
    private Set<PermissionResponse> permissions;

    public RoleResponse(Long id, String name, String description, Set<PermissionResponse> permissions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<PermissionResponse> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionResponse> permissions) {
        this.permissions = permissions;
    }

}
