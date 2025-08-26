package com.gestionremodelacion.gestion.empleado.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.empleado.dto.request.EmpleadoRequest;
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoExportDTO;
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoResponse;
import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.repository.EmpleadoRepository;
import com.gestionremodelacion.gestion.mapper.EmpleadoMapper;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final EmpleadoMapper empleadoMapper;

    public EmpleadoService(EmpleadoRepository empleadoRepository, EmpleadoMapper empleadoMapper) {
        this.empleadoRepository = empleadoRepository;
        this.empleadoMapper = empleadoMapper;
    }

    @Transactional(readOnly = true)
    public Page<EmpleadoResponse> getAllEmpleados(Pageable pageable, String filter) {
        Page<Empleado> empleadosPage;
        if (filter != null && !filter.trim().isEmpty()) {
            empleadosPage = empleadoRepository.findByNombreCompletoContainingIgnoreCaseOrRolCargoContainingIgnoreCaseOrTelefonoContactoContainingIgnoreCase(filter, filter, filter, pageable);
        } else {
            empleadosPage = empleadoRepository.findAll(pageable);
        }
        return empleadosPage.map(empleadoMapper::toEmpleadoResponse);
    }

    @Transactional(readOnly = true)
    public EmpleadoResponse getEmpleadoById(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado con ID: " + id));
        return empleadoMapper.toEmpleadoResponse(empleado);
    }

    @Transactional
    public EmpleadoResponse createEmpleado(EmpleadoRequest empleadoRequest) {
        Empleado empleado = empleadoMapper.toEmpleado(empleadoRequest);
        if (empleado.getActivo() == null) {
            empleado.setActivo(true);
        }
        // fechaRegistro will be set automatically by @PrePersist in Empleado model
        Empleado savedEmpleado = empleadoRepository.save(empleado);
        return empleadoMapper.toEmpleadoResponse(savedEmpleado);
    }

    @Transactional
    public EmpleadoResponse updateEmpleado(Long id, EmpleadoRequest empleadoRequest) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado con ID: " + id));

        empleadoMapper.updateEmpleadoFromRequest(empleadoRequest, empleado);
        Empleado updatedEmpleado = empleadoRepository.save(empleado);
        return empleadoMapper.toEmpleadoResponse(updatedEmpleado);
    }

    @Transactional
    public ApiResponse<Void> changeEmpleadoStatus(Long id, Boolean activo) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado con ID: " + id));

        if (empleado.getActivo().equals(activo)) {
            return ApiResponse.error(HttpStatus.CONFLICT.value(), "El empleado ya tiene el estado deseado.");
        }

        empleado.setActivo(activo);
        empleadoRepository.save(empleado);
        String status = activo ? "activado" : "inactivado";
        return new ApiResponse<>(HttpStatus.OK.value(), "Empleado " + status + " exitosamente.", null);
    }

    @Transactional
    public ApiResponse<Void> deactivateEmpleado(Long id) {
        return changeEmpleadoStatus(id, false);
    }

    @Transactional
    public ApiResponse<Void> deleteEmpleado(Long id) {
        if (!empleadoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado con ID: " + id);
        }
        empleadoRepository.deleteById(id);
        return new ApiResponse<>(HttpStatus.OK.value(), "Empleado eliminado físicamente de la base de datos.", null);
    }

    // ⭐️ NUEVO: Método para la exportación
    @Transactional(readOnly = true)
    public List<EmpleadoExportDTO> findEmpleadosForExport(String filter, String sort) {
        Sort sortObj = Sort.by(Sort.Direction.ASC, "nombreCompleto"); // Orden por defecto
        if (sort != null && !sort.isEmpty()) {
            try {
                String[] sortParts = sort.split(",");
                if (sortParts.length == 2) {
                    String sortProperty = sortParts[0];
                    Sort.Direction sortDirection = "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
                    sortObj = Sort.by(sortDirection, sortProperty);
                }
            } catch (Exception e) {
                // Manejar error de parseo, si es necesario, o mantener el sort por defecto
            }
        }

        List<Empleado> empleados;
        if (filter != null && !filter.trim().isEmpty()) {
            empleados = empleadoRepository.findByNombreCompletoContainingIgnoreCaseOrRolCargoContainingIgnoreCaseOrTelefonoContactoContainingIgnoreCase(filter, filter, filter, sortObj);
        } else {
            empleados = empleadoRepository.findAll(sortObj);
        }

        return empleados.stream()
                .map(EmpleadoExportDTO::new)
                .collect(Collectors.toList());
    }

}
