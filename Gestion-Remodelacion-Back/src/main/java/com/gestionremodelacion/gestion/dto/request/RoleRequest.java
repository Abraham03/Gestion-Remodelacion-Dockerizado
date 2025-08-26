package com.gestionremodelacion.gestion.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RoleRequest {

    private Long id;

    @NotBlank(message = "Role name cannot be empty")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Set<Long> permissions; // Names of associated permissions

    // Constructors
    public RoleRequest() {
    }

    // Getters and Setters
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

    public Set<Long> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Long> permissions) {
        this.permissions = permissions;
    }
}
