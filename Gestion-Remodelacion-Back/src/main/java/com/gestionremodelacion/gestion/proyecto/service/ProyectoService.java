package com.gestionremodelacion.gestion.proyecto.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.cliente.model.Cliente;
import com.gestionremodelacion.gestion.cliente.repository.ClienteRepository;
import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.repository.EmpleadoRepository;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.mapper.ProyectoMapper;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.proyecto.dto.request.ProyectoRequest;
import com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoExcelDTO;
import com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoPdfDTO;
import com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoResponse;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto;
import com.gestionremodelacion.gestion.proyecto.repository.ProyectoRepository;
import com.gestionremodelacion.gestion.service.user.UserService;

@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final ProyectoMapper proyectoMapper;
    private final UserService userService;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;

    public ProyectoService(ProyectoRepository proyectoRepository, ProyectoMapper proyectoMapper,
            UserService userService, ClienteRepository clienteRepository, EmpleadoRepository empleadoRepository) {
        this.proyectoRepository = proyectoRepository;
        this.proyectoMapper = proyectoMapper;
        this.userService = userService;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProyectoResponse> getAllProyectos(Pageable pageable, String filter) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        String effectiveFilter = (filter != null && !filter.trim().isEmpty()) ? filter.trim().toLowerCase() : null;

        return proyectoRepository.findByFilterWithDetails(empresaId, effectiveFilter, pageable);
    }

    @Transactional(readOnly = true)
    public ProyectoResponse getProyectoById(Long id) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        return proyectoRepository.findByIdAndEmpresaId(id, empresaId)
                .map(proyectoMapper::toProyectoResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.PROJECT_NOT_FOUND.getKey()));
    }

    @Transactional
    public ProyectoResponse createProyecto(ProyectoRequest proyectoRequest) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Cliente cliente = clienteRepository.findByIdAndEmpresaId(proyectoRequest.getIdCliente(), empresaId)
                .orElseThrow(() -> new BusinessRuleException(
                        ErrorCatalog.INVALID_CLIENT_FOR_COMPANY.getKey()));

        Empleado empleadoResponsable = null;
        if (proyectoRequest.getIdEmpleadoResponsable() != null) {
            empleadoResponsable = empleadoRepository
                    .findByIdAndEmpresaId(proyectoRequest.getIdEmpleadoResponsable(), empresaId)
                    .orElseThrow(() -> new BusinessRuleException(
                            ErrorCatalog.INVALID_EMPLOYEE_FOR_COMPANY.getKey()));
        }
        // 1. El mapper ya debería transferir todos los valores del request,
        // incluyendo montoRecibido, costoMateriales, etc.
        Proyecto proyecto = proyectoMapper.toProyecto(proyectoRequest);
        // 2. Asigna las entidades relacionadas
        proyecto.setEmpresa(currentUser.getEmpresa());
        proyecto.setCliente(cliente);
        proyecto.setEmpleadoResponsable(empleadoResponsable);

        // 3. Asignar valores por defecto si no se proporcionan
        proyecto.setMontoRecibido(proyectoRequest.getMontoRecibido() != null
                ? proyectoRequest.getMontoRecibido()
                : BigDecimal.ZERO);

        proyecto.setCostoMaterialesConsolidado(proyectoRequest.getCostoMaterialesConsolidado() != null
                ? proyectoRequest.getCostoMaterialesConsolidado()
                : BigDecimal.ZERO);
        proyecto.setOtrosGastosDirectosConsolidado(proyectoRequest.getOtrosGastosDirectosConsolidado() != null
                ? proyectoRequest.getOtrosGastosDirectosConsolidado()
                : BigDecimal.ZERO);
        // El costo de mano de obra se calcula por separado, así que es correcto
        // inicializarlo en cero.
        proyecto.setCostoManoDeObra(BigDecimal.ZERO);

        Proyecto savedProyecto = proyectoRepository.save(proyecto);
        return proyectoMapper.toProyectoResponse(savedProyecto);
    }

    @Transactional
    public ProyectoResponse updateProyecto(Long id, ProyectoRequest proyectoRequest) {

        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Proyecto proyecto = proyectoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.PROJECT_NOT_FOUND.getKey()));

        Cliente cliente = clienteRepository.findByIdAndEmpresaId(proyectoRequest.getIdCliente(), empresaId)
                .orElseThrow(() -> new BusinessRuleException(
                        ErrorCatalog.INVALID_CLIENT_FOR_COMPANY.getKey()));

        Empleado empleadoResponsable = null;
        if (proyectoRequest.getIdEmpleadoResponsable() != null) {
            empleadoResponsable = empleadoRepository
                    .findByIdAndEmpresaId(proyectoRequest.getIdEmpleadoResponsable(), empresaId)
                    .orElseThrow(() -> new BusinessRuleException(
                            ErrorCatalog.INVALID_EMPLOYEE_FOR_COMPANY.getKey()));
        }

        proyectoMapper.updateProyectoFromRequest(proyectoRequest, proyecto);
        proyecto.setCliente(cliente);
        proyecto.setEmpleadoResponsable(empleadoResponsable);

        Proyecto updatedProyecto = proyectoRepository.save(proyecto);
        return proyectoMapper.toProyectoResponse(updatedProyecto);
    }

    @Transactional
    public void deleteProyecto(Long id) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        // 1. Verificar si el proyecto pertenece a la empresa actual
        if (!proyectoRepository.existsByIdAndEmpresaId(id, empresaId)) {
            throw new ResourceNotFoundException(ErrorCatalog.PROJECT_NOT_FOUND.getKey());
        }

        // 7. Si no hay dependencias, proceder a eliminar
        proyectoRepository.deleteById(id);
    }

    @Transactional
    private List<Proyecto> findAllProyectosForExport(String filter, String sort) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Sort sortObj = Sort.by(Sort.Direction.ASC, "nombreProyecto");
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String property = sortParts[0];
                Sort.Direction direction = "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                sortObj = Sort.by(direction, property);
            }
        }

        return (filter != null && !filter.trim().isEmpty())
                ? proyectoRepository.findByFilterForExport(empresaId, filter, sortObj)
                : proyectoRepository.findAll(sortObj);
    }

    @Transactional(readOnly = true)
    public List<ProyectoPdfDTO> findProyectosForPdfExport(String filter, String sort) {
        List<Proyecto> proyectos = findAllProyectosForExport(filter, sort);
        return proyectos.stream()
                .map(ProyectoPdfDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProyectoExcelDTO> findProyectosForExcelExport(String filter, String sort) {
        List<Proyecto> proyectos = findAllProyectosForExport(filter, sort);
        return proyectos.stream()
                .map(ProyectoExcelDTO::new)
                .collect(Collectors.toList());
    }

}
