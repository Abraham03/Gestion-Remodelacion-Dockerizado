package com.gestionremodelacion.gestion.cliente.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.cliente.dto.request.ClienteRequest;
import com.gestionremodelacion.gestion.cliente.dto.response.ClienteExportDTO;
import com.gestionremodelacion.gestion.cliente.dto.response.ClienteResponse;
import com.gestionremodelacion.gestion.cliente.model.Cliente;
import com.gestionremodelacion.gestion.cliente.repository.ClienteRepository;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.mapper.ClienteMapper;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> getAllClientes(Pageable pageable, String filter) {
        Page<Cliente> clientesPage = (filter != null && !filter.trim().isEmpty())
                ? clienteRepository.findByNombreClienteContainingIgnoreCaseOrTelefonoContactoContainingIgnoreCase(
                        filter, filter, pageable)
                : clienteRepository.findAll(pageable);
        return clientesPage.map(clienteMapper::toClienteResponse);
    }

    @Transactional(readOnly = true)
    public ClienteResponse getClienteById(Long id) {
        return clienteRepository.findById(id)
                .map(clienteMapper::toClienteResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
    }

    @Transactional
    public ClienteResponse createCliente(ClienteRequest clienteRequest) {
        Cliente cliente = clienteMapper.toCliente(clienteRequest);
        Cliente savedCliente = clienteRepository.save(cliente);
        return clienteMapper.toClienteResponse(savedCliente);
    }

    @Transactional
    public ClienteResponse updateCliente(Long id, ClienteRequest clienteRequest) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        clienteMapper.updateClienteFromRequest(clienteRequest, cliente);
        Cliente updatedCliente = clienteRepository.save(cliente);
        return clienteMapper.toClienteResponse(updatedCliente);
    }

    @Transactional
    public void deleteCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado con ID: " + id);
        }
        clienteRepository.deleteById(id);
    }

    // ⭐️ Nuevo método para exportación
    @Transactional(readOnly = true)
    public List<ClienteExportDTO> findClientesForExport(String filter, String sort) {
        Sort sortObj = Sort.by(Sort.Direction.ASC, "nombreCliente"); // Orden por defecto
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            String sortProperty = sortParts[0];
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sortObj = Sort.by(sortDirection, sortProperty);
        }

        List<Cliente> clientes;
        if (filter != null && !filter.trim().isEmpty()) {
            // Lógica de filtrado y ordenamiento en el repositorio.
            // Es necesario añadir un método en ClienteRepository para esto.
            clientes = clienteRepository.findByNombreClienteContainingIgnoreCaseOrTelefonoContactoContainingIgnoreCase(
                    filter, filter, sortObj);
        } else {
            clientes = clienteRepository.findAll(sortObj);
        }

        return clientes.stream()
                .map(ClienteExportDTO::new)
                .collect(Collectors.toList());
    }

}
