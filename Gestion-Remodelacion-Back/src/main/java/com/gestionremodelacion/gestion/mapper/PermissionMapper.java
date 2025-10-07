package com.gestionremodelacion.gestion.mapper;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.gestionremodelacion.gestion.dto.request.PermissionRequest;
import com.gestionremodelacion.gestion.dto.response.PermissionResponse;
import com.gestionremodelacion.gestion.model.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    // Convierte entidad Permission a PermissionResponse DTO
    PermissionResponse toPermissionResponse(Permission permission);

    // Convierte un Set de entidades Permission a un Set de PermissionResponse DTOs
    Set<PermissionResponse> toPermissionResponseSet(Set<Permission> permissions);

    // Convierte PermissionRequest DTO a entidad Permission (para
    // creación/actualización)
    // MapStruct puede manejar esto automáticamente
    Permission toEntity(PermissionRequest permissionRequest);

    // Opcional: Para actualizar una entidad existente desde un request DTO
    void updateEntityFromDto(PermissionRequest dto, @MappingTarget Permission entity);
}
