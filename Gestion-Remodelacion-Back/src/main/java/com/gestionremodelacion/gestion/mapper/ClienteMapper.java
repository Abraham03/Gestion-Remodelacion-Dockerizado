package com.gestionremodelacion.gestion.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.gestionremodelacion.gestion.cliente.dto.request.ClienteRequest;
import com.gestionremodelacion.gestion.cliente.dto.response.ClienteResponse;
import com.gestionremodelacion.gestion.cliente.model.Cliente;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    /**
     * Maps a Cliente entity to a ClienteResponse DTO. Direct field mapping is
     * performed.
     *
     * @param cliente The Cliente entity to map.
     * @return The mapped ClienteResponse DTO.
     */
    ClienteResponse toClienteResponse(Cliente cliente);

    /**
     * Maps a list of Cliente entities to a list of ClienteResponse DTOs.
     *
     * @param clientes The list of Cliente entities to map.
     * @return The list of mapped ClienteResponse DTOs.
     */
    List<ClienteResponse> toClienteResponseList(List<Cliente> clientes);

    /**
     * Maps a ClienteRequest DTO to a new Cliente entity. 'id' and
     * 'fechaRegistro' are ignored as they are handled by the database/entity
     * lifecycle.
     *
     * @param request The ClienteRequest DTO to map from.
     * @return A new Cliente entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    Cliente toCliente(ClienteRequest request);

    /**
     * Updates an existing Cliente entity from a ClienteRequest DTO. 'id' and
     * 'fechaRegistro' are ignored as they should not be updated from the
     * request.
     *
     * @param request The ClienteRequest DTO with updated data.
     * @param cliente The existing Cliente entity to update.
     */
    @Mapping(target = "id", ignore = true) // ID should not be updated from request
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    void updateClienteFromRequest(ClienteRequest request, @MappingTarget Cliente cliente);
}
