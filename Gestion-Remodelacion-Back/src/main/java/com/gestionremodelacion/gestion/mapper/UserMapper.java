package com.gestionremodelacion.gestion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.gestionremodelacion.gestion.dto.request.UserRequest;
import com.gestionremodelacion.gestion.dto.response.UserResponse;
import com.gestionremodelacion.gestion.model.User;

@Mapper(componentModel = "spring", uses = {RoleMapper.class}) // Añadir uses para RoleMapper
public abstract class UserMapper {

    @Autowired
    protected RoleMapper roleMapper; // Inyectar RoleMapper

    // Convierte entidad User a UserResponse DTO
    @Mapping(target = "roles", source = "roles") // Directly map the roles set
    public abstract UserResponse toDto(User user);

    // Convierte UserRequest DTO a entidad User (para creación/actualización)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // La contraseña se codificará en el servicio
    @Mapping(target = "roles", ignore = true) // Los roles se asignarán en el servicio
    public abstract User toEntity(UserRequest userRequest);
}
