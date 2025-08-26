package com.gestionremodelacion.gestion.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.repository.EmpleadoRepository;
import com.gestionremodelacion.gestion.horastrabajadas.dto.request.HorasTrabajadasRequest;
import com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasResponse;
import com.gestionremodelacion.gestion.horastrabajadas.model.HorasTrabajadas;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto;
import com.gestionremodelacion.gestion.proyecto.repository.ProyectoRepository;

@Mapper(componentModel = "spring")
public abstract class HorasTrabajadasMapper {

    @Autowired
    protected EmpleadoRepository empleadoRepository; // Inyectar para buscar Empleado por ID
    @Autowired
    protected ProyectoRepository proyectoRepository; // Inyectar para buscar Proyecto por ID

    /**
     * Mapea una entidad HorasTrabajadas a un DTO HorasTrabajadasResponse.
     * Incluye los IDs y nombres del empleado y proyecto relacionados.
     *
     * @param horasTrabajadas La entidad HorasTrabajadas a mapear.
     * @return El DTO HorasTrabajadasResponse mapeado.
     */
    @Mapping(source = "empleado.id", target = "idEmpleado")
    @Mapping(source = "empleado.nombreCompleto", target = "nombreEmpleado")
    @Mapping(source = "proyecto.id", target = "idProyecto")
    @Mapping(source = "proyecto.nombreProyecto", target = "nombreProyecto")
    public abstract HorasTrabajadasResponse toHorasTrabajadasResponse(HorasTrabajadas horasTrabajadas);

    /**
     * Mapea una lista de entidades HorasTrabajadas a una lista de DTOs
     * HorasTrabajadasResponse.
     *
     * @param horasTrabajadasList La lista de entidades HorasTrabajadas a
     * mapear.
     * @return La lista de DTOs HorasTrabajadasResponse mapeados.
     */
    public abstract List<HorasTrabajadasResponse> toHorasTrabajadasResponseList(List<HorasTrabajadas> horasTrabajadasList);

    /**
     * Mapea un DTO HorasTrabajadasRequest a una nueva entidad HorasTrabajadas.
     * Resuelve las entidades Empleado y Proyecto usando los IDs del request.
     * 'id' y 'fechaRegistro' son ignorados ya que son manejados por la base de
     * datos/ciclo de vida de la entidad.
     *
     * @param request El DTO HorasTrabajadasRequest a mapear.
     * @return Una nueva entidad HorasTrabajadas.
     */
    @Mapping(target = "id", ignore = true) // El ID es generado por la DB
    @Mapping(target = "fechaRegistro", ignore = true) // Manejado por @PrePersist en la entidad
    @Mapping(target = "empleado", expression = "java(getEmpleadoById(request.getIdEmpleado()))")
    @Mapping(target = "proyecto", expression = "java(getProyectoById(request.getIdProyecto()))")
    public abstract HorasTrabajadas toHorasTrabajadas(HorasTrabajadasRequest request);

    /**
     * Actualiza una entidad HorasTrabajadas existente desde un DTO
     * HorasTrabajadasRequest. Resuelve las entidades Empleado y Proyecto usando
     * los IDs del request si han cambiado. 'id' y 'fechaRegistro' son ignorados
     * ya que no deben ser actualizados desde el request.
     *
     * @param request El DTO HorasTrabajadasRequest con los datos actualizados.
     * @param horasTrabajadas La entidad HorasTrabajadas existente a actualizar.
     */
    @Mapping(target = "id", ignore = true) // El ID no debe ser actualizado desde el request
    @Mapping(target = "fechaRegistro", ignore = true) // FechaRegistro no debe ser actualizada desde el request
    @Mapping(target = "empleado", expression = "java(getEmpleadoById(request.getIdEmpleado()))") // Siempre intentamos resolver
    @Mapping(target = "proyecto", expression = "java(getProyectoById(request.getIdProyecto()))") // Siempre intentamos resolver
    public abstract void updateHorasTrabajadasFromRequest(HorasTrabajadasRequest request, @MappingTarget HorasTrabajadas horasTrabajadas);

    // --- Métodos auxiliares para buscar entidades por ID ---
    /**
     * Busca una entidad Empleado por su ID.
     *
     * @param idEmpleado El ID del empleado a buscar.
     * @return La entidad Empleado encontrada.
     * @throws ResponseStatusException Si el empleado no es encontrado.
     */
    protected Empleado getEmpleadoById(Long idEmpleado) {
        if (idEmpleado == null) {
            // Decidir si es un error o si se permite un empleado nulo (para un PUT/PATCH parcial)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del empleado no puede ser nulo.");
        }
        return empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado con ID: " + idEmpleado));
    }

    /**
     * Busca una entidad Proyecto por su ID.
     *
     * @param idProyecto El ID del proyecto a buscar.
     * @return La entidad Proyecto encontrada.
     * @throws ResponseStatusException Si el proyecto no es encontrado.
     */
    protected Proyecto getProyectoById(Long idProyecto) {
        if (idProyecto == null) {
            // Decidir si es un error o si se permite un proyecto nulo (para un PUT/PATCH parcial)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del proyecto no puede ser nulo.");
        }
        return proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado con ID: " + idProyecto));
    }

}
