package com.gestionremodelacion.gestion.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import com.gestionremodelacion.gestion.dto.request.RoleRequest;
import com.gestionremodelacion.gestion.dto.response.RoleResponse;
import com.gestionremodelacion.gestion.model.Role;
import com.gestionremodelacion.gestion.repository.PermissionRepository;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public abstract class RoleMapper {

    @Autowired
    protected PermissionRepository permissionRepository; // Inyectar PermissionRepository

    @Autowired
    protected PermissionMapper permissionMapper;

    @Mapping(target = "permissions", source = "permissions")
    public abstract RoleResponse toRoleResponse(Role role);

    public abstract List<RoleResponse> toRoleResponseList(List<Role> roles);

    @Mapping(target = "id", ignore = true) // ID es generado por la DB
    @Mapping(target = "permissions", ignore = true) // Los permisos se asignan en el servicio
    public abstract Role toRole(RoleRequest request);

    @Mapping(target = "id", ignore = true) // El ID no se actualiza desde el request
    @Mapping(target = "permissions", ignore = true) // Los permisos se actualizan en el servicio
    public abstract void updateRoleFromRequest(RoleRequest request, @MappingTarget Role role);

}
