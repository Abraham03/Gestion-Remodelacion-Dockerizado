package com.gestionremodelacion.gestion.empleado.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.empleado.dto.request.EmpleadoRequest;
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoExportDTO;
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoResponse;
import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.repository.EmpleadoRepository;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.mapper.EmpleadoMapper;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.service.user.UserService;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final EmpleadoMapper empleadoMapper;
    private final UserService userService;

    public EmpleadoService(EmpleadoRepository empleadoRepository, EmpleadoMapper empleadoMapper,
            UserService userService) {
        this.empleadoRepository = empleadoRepository;
        this.empleadoMapper = empleadoMapper;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<EmpleadoResponse> getAllEmpleados(Pageable pageable, String filter) {
        User user = userService.getCurrentUser();
        Long empresaId = user.getEmpresa().getId();

        Page<Empleado> empleadosPage = empleadoRepository.findByEmpresaIdAndFilter(empresaId, filter, pageable);

        return empleadosPage.map(empleadoMapper::toEmpleadoResponse);
    }

    @Transactional(readOnly = true)
    public EmpleadoResponse getEmpleadoById(Long id) {
        User user = userService.getCurrentUser();
        Long empresaId = user.getEmpresa().getId();

        return empleadoRepository.findByIdAndEmpresaId(id, empresaId)
                .map(empleadoMapper::toEmpleadoResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));
    }

    @Transactional
    public EmpleadoResponse createEmpleado(EmpleadoRequest empleadoRequest) {
        User currentUser = userService.getCurrentUser();

        Empleado empleado = empleadoMapper.toEmpleado(empleadoRequest);
        empleado.setEmpresa(currentUser.getEmpresa());

        if (empleado.getActivo() == null) {
            empleado.setActivo(true);
        }
        Empleado savedEmpleado = empleadoRepository.save(empleado);
        return empleadoMapper.toEmpleadoResponse(savedEmpleado);
    }

    @Transactional
    public EmpleadoResponse updateEmpleado(Long id, EmpleadoRequest empleadoRequest) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Empleado empleado = empleadoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));

        empleadoMapper.updateEmpleadoFromRequest(empleadoRequest, empleado);
        Empleado updatedEmpleado = empleadoRepository.save(empleado);
        return empleadoMapper.toEmpleadoResponse(updatedEmpleado);
    }

    @Transactional
    public void changeEmpleadoStatus(Long id, Boolean activo) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Empleado empleado = empleadoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));
        if (empleado.getActivo().equals(activo)) {
            throw new BusinessRuleException("El empleado ya tiene el estado deseado.");
        }
        empleado.setActivo(activo);
        empleadoRepository.save(empleado);
    }

    @Transactional
    public void deleteEmpleado(Long id) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        if (!empleadoRepository.existsByIdAndEmpresaId(id, empresaId)) {
            throw new ResourceNotFoundException("Empleado no encontrado con ID: " + id);
        }
        empleadoRepository.deleteById(id);
    }

    // Método para la exportación
    @Transactional(readOnly = true)
    public List<EmpleadoExportDTO> findEmpleadosForExport(String filter, String sort) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Sort sortObj = Sort.by(Sort.Direction.ASC, "nombreCompleto"); // Orden por defecto
        if (sort != null && !sort.isEmpty()) {
            try {
                String[] sortParts = sort.split(",");
                if (sortParts.length == 2) {
                    String sortProperty = sortParts[0];
                    Sort.Direction sortDirection = "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
                    sortObj = Sort.by(sortDirection, sortProperty);
                }
            } catch (Exception e) {
                // Manejar error de parseo, si es necesario, o mantener el sort por defecto
            }
        }

        List<Empleado> empleados = empleadoRepository.findByEmpresaIdAndFilter(empresaId, filter, sortObj);

        return empleados.stream()
                .map(EmpleadoExportDTO::new)
                .collect(Collectors.toList());
    }

}
