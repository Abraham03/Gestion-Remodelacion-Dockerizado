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
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.mapper.ClienteMapper;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.service.user.UserService;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final UserService userService;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper, UserService userService) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> getAllClientes(Pageable pageable, String filter) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Page<Cliente> clientesPage = (filter != null && !filter.trim().isEmpty())
                ? clienteRepository
                        .findByEmpresaIdAndNombreClienteContainingIgnoreCaseOrEmpresaIdAndTelefonoContactoContainingIgnoreCase(
                                empresaId, filter, empresaId, filter, pageable)
                : clienteRepository.findAllByEmpresaId(empresaId, pageable); // Usa el nuevo método del repositorio
        return clientesPage.map(clienteMapper::toClienteResponse);
    }

    @Transactional(readOnly = true)
    public ClienteResponse getClienteById(Long id) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        return clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .map(clienteMapper::toClienteResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.RESOURCE_NOT_FOUND.getKey()));
    }

    @Transactional
    public ClienteResponse createCliente(ClienteRequest clienteRequest) {
        User currentUser = userService.getCurrentUser();

        Cliente cliente = clienteMapper.toCliente(clienteRequest);
        cliente.setEmpresa(currentUser.getEmpresa());

        Cliente savedCliente = clienteRepository.save(cliente);
        return clienteMapper.toClienteResponse(savedCliente);
    }

    @Transactional
    public ClienteResponse updateCliente(Long id, ClienteRequest clienteRequest) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Cliente cliente = clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.RESOURCE_NOT_FOUND.getKey()));

        clienteMapper.updateClienteFromRequest(clienteRequest, cliente);
        Cliente updatedCliente = clienteRepository.save(cliente);
        return clienteMapper.toClienteResponse(updatedCliente);
    }

    @Transactional
    public void deleteCliente(Long id) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        if (!clienteRepository.existsByIdAndEmpresaId(id, empresaId)) {
            throw new ResourceNotFoundException(ErrorCatalog.RESOURCE_NOT_FOUND.getKey());
        }
        clienteRepository.deleteById(id);
    }

    // método para exportación
    @Transactional(readOnly = true)
    public List<ClienteExportDTO> findClientesForExport(String filter, String sort) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

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
            clientes = clienteRepository
                    .findByEmpresaIdAndNombreClienteContainingIgnoreCaseOrEmpresaIdAndTelefonoContactoContainingIgnoreCase(
                            empresaId, filter, empresaId, filter, sortObj);
        } else {
            clientes = clienteRepository.findAll(sortObj);
        }

        return clientes.stream()
                .map(ClienteExportDTO::new)
                .collect(Collectors.toList());
    }

}
