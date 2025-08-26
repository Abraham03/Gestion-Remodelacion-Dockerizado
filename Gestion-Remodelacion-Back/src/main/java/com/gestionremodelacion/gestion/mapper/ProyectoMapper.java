package com.gestionremodelacion.gestion.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.gestionremodelacion.gestion.cliente.model.Cliente;
import com.gestionremodelacion.gestion.cliente.repository.ClienteRepository;
import com.gestionremodelacion.gestion.dto.response.ProyectoDashboardDto;
import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.repository.EmpleadoRepository;
import com.gestionremodelacion.gestion.proyecto.dto.request.ProyectoRequest;
import com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoResponse;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto;

@Mapper(componentModel = "spring")
public abstract class ProyectoMapper {

    // NEW: Inject repositories into the mapper
    @Autowired
    protected ClienteRepository clienteRepository; // Use protected for access in generated class
    @Autowired
    protected EmpleadoRepository empleadoRepository; // Use protected for access in generated class

    // Mapping for Dashboard
    @Mapping(target = "estado", expression = "java(proyecto.getEstado().toString())")
    public abstract ProyectoDashboardDto toProyectoDashboardDto(Proyecto proyecto);

    public abstract List<ProyectoDashboardDto> toProyectoDashboardDtoList(List<Proyecto> proyectos);

    // Mapping for ProyectoResponse
    @Mapping(source = "cliente.id", target = "idCliente")
    @Mapping(source = "cliente.nombreCliente", target = "nombreCliente")
    @Mapping(source = "empleadoResponsable.id", target = "idEmpleadoResponsable")
    @Mapping(source = "empleadoResponsable.nombreCompleto", target = "nombreEmpleadoResponsable")
    public abstract ProyectoResponse toProyectoResponse(Proyecto proyecto);

    public abstract List<ProyectoResponse> toProyectoResponseList(List<Proyecto> proyectos);

    /**
     * Maps a ProyectoRequest to a new Proyecto entity. Handles fetching Cliente
     * and Empleado entities based on IDs.
     */
    @Mapping(target = "cliente", expression = "java(getClienteById(request.getIdCliente()))")
    @Mapping(target = "empleadoResponsable", expression = "java(getEmpleadoById(request.getIdEmpleadoResponsable()))")
    @Mapping(target = "id", ignore = true) // Ignore ID for new entity creation
    @Mapping(target = "fechaCreacion", ignore = true) // Assume fechaCreacion is set by @CreatedDate or similar
    public abstract Proyecto toProyecto(ProyectoRequest request);

    /**
     * Updates an existing Proyecto entity from a ProyectoRequest. Handles
     * fetching Cliente and Empleado entities based on IDs.
     */
    @Mapping(target = "cliente", expression = "java(getClienteById(request.getIdCliente()))")
    @Mapping(target = "empleadoResponsable", expression = "java(getEmpleadoById(request.getIdEmpleadoResponsable()))")
    @Mapping(target = "id", ignore = true) // ID should not be updated from request
    @Mapping(target = "fechaCreacion", ignore = true) // FechaCreacion should not be updated from request
    public abstract void updateProyectoFromRequest(ProyectoRequest request, @MappingTarget Proyecto proyecto);

    // Helper methods to fetch Cliente and Empleado, used in expressions above
    protected Cliente getClienteById(Long idCliente) {
        if (idCliente == null) {
            return null; // Or throw an exception if cliente is always required
        }
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente no encontrado con ID: " + idCliente));
    }

    protected Empleado getEmpleadoById(Long idEmpleadoResponsable) {
        if (idEmpleadoResponsable == null) {
            return null;
        }
        return empleadoRepository.findById(idEmpleadoResponsable)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empleado responsable no encontrado con ID: " + idEmpleadoResponsable));
    }

}
