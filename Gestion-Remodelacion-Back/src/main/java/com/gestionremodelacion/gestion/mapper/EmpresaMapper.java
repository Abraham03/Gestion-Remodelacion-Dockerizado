package com.gestionremodelacion.gestion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.gestionremodelacion.gestion.empresa.dto.EmpresaRequest;
import com.gestionremodelacion.gestion.empresa.dto.EmpresaResponse;
import com.gestionremodelacion.gestion.empresa.model.Empresa;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    @Mapping(target = "telefono", source = "telefono")
    EmpresaResponse toDto(Empresa empresa);

    // Convierte DTO de Petición -> Nueva Entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    Empresa toEntity(EmpresaRequest empresaRequest);

    // Convierte Entidad -> DTO de Respuesta
    EmpresaResponse toSimpleDto(Empresa empresa);

    // Actualiza una Entidad existente desde un DTO de Petición
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    void updateEntityFromDto(EmpresaRequest dto, @MappingTarget Empresa entity);
}
