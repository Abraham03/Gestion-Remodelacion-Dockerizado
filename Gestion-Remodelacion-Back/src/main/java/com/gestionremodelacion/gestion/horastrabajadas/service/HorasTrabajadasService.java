package com.gestionremodelacion.gestion.horastrabajadas.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.model.ModeloDePago;
import com.gestionremodelacion.gestion.empleado.repository.EmpleadoRepository;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.horastrabajadas.dto.request.HorasTrabajadasRequest;
import com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasExportDTO;
import com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasResponse;
import com.gestionremodelacion.gestion.horastrabajadas.model.HorasTrabajadas;
import com.gestionremodelacion.gestion.horastrabajadas.repository.HorasTrabajadasRepository;
import com.gestionremodelacion.gestion.mapper.HorasTrabajadasMapper;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto;
import com.gestionremodelacion.gestion.proyecto.repository.ProyectoRepository;
import com.gestionremodelacion.gestion.service.user.UserService;

@Service
public class HorasTrabajadasService {

    private final HorasTrabajadasRepository horasTrabajadasRepository;
    private final HorasTrabajadasMapper horasTrabajadasMapper;
    private final EmpleadoRepository empleadoRepository;
    private final ProyectoRepository proyectoRepository;
    private final UserService userService;

    private static final String PERMISO_READ_ALL = "HORASTRABAJADAS_READ_ALL";
    private static final String PERMISO_CREATE_ALL = "HORASTRABAJADAS_CREATE_ALL";
    private static final String PERMISO_UPDATE_ALL = "HORASTRABAJADAS_UPDATE_ALL";
    private static final String PERMISO_DELETE_ALL = "HORASTRABAJADAS_DELETE_ALL";

    public HorasTrabajadasService(HorasTrabajadasRepository horasTrabajadasRepository,
            HorasTrabajadasMapper horasTrabajadasMapper, EmpleadoRepository empleadoRepository,
            ProyectoRepository proyectoRepository, UserService userService) {
        this.horasTrabajadasRepository = horasTrabajadasRepository;
        this.horasTrabajadasMapper = horasTrabajadasMapper;
        this.empleadoRepository = empleadoRepository;
        this.proyectoRepository = proyectoRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<HorasTrabajadasResponse> getAllHorasTrabajadas(Pageable pageable, String filter) {

        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        // Logica de seguridad
        boolean canReadAll = hasPermission(currentUser, PERMISO_READ_ALL);

        if (canReadAll) {
            // El usuario es Admin/Manager, usa la consulta antigua (ver todo)
            return (filter != null && !filter.trim().isEmpty())
                    ? horasTrabajadasRepository.findByFilterWithDetails(empresaId, filter, pageable)
                    : horasTrabajadasRepository.findAllWithDetails(empresaId, pageable);
        } else {
            // El usuario es un trabajador, usa la nueva consulta (ver solo lo suyo)
            Empleado empleadoVinculado = getEmpleadoVinculado(currentUser); // Método helper

            return (filter != null && !filter.trim().isEmpty())
                    ? horasTrabajadasRepository.findByFilterWithDetailsByEmpleado(empresaId, empleadoVinculado.getId(),
                            filter, pageable)
                    : horasTrabajadasRepository.findAllWithDetailsByEmpleado(empresaId, empleadoVinculado.getId(),
                            pageable);
        }
    }

    @Transactional(readOnly = true)
    public HorasTrabajadasResponse getHorasTrabajadasById(Long id) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        HorasTrabajadas horasTrabajadas = horasTrabajadasRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.RESOURCE_NOT_FOUND.getKey()));

        // --- LÓGICA DE SEGURIDAD CORREGIDA ---
        boolean canReadAll = hasPermission(currentUser, PERMISO_READ_ALL);

        if (canReadAll) {
            return horasTrabajadasMapper.toHorasTrabajadasResponse(horasTrabajadas);
        }

        Empleado empleadoVinculado = getEmpleadoVinculado(currentUser);
        if (horasTrabajadas.getEmpleado().getId().equals(empleadoVinculado.getId())) {
            return horasTrabajadasMapper.toHorasTrabajadasResponse(horasTrabajadas);
        }

        // Si no es admin y no es el dueño, no lo encuentra
        throw new ResourceNotFoundException(ErrorCatalog.RESOURCE_NOT_FOUND.getKey());
    }

    @Transactional
    public HorasTrabajadasResponse createHorasTrabajadas(HorasTrabajadasRequest horasTrabajadasRequest) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        // --- LÓGICA DE SEGURIDAD ---
        boolean canCreateForAll = hasPermission(currentUser, PERMISO_CREATE_ALL);

        Long empleadoIdParaRegistro;

        if (canCreateForAll) {
            // Admin usa el ID del request
            empleadoIdParaRegistro = horasTrabajadasRequest.getIdEmpleado();
        } else {
            // Trabajador usa su propio ID, ignorando el request
            empleadoIdParaRegistro = getEmpleadoVinculado(currentUser).getId();
        }

        // Validar entidades
        Empleado empleado = empleadoRepository.findByIdAndEmpresaId(empleadoIdParaRegistro, empresaId)
                .orElseThrow(() -> new BusinessRuleException(
                        ErrorCatalog.INVALID_EMPLOYEE_FOR_COMPANY.getKey()));

        Proyecto proyecto = proyectoRepository.findByIdAndEmpresaId(horasTrabajadasRequest.getIdProyecto(), empresaId)
                .orElseThrow(() -> new BusinessRuleException(
                        ErrorCatalog.INVALID_PROJECT_FOR_COMPANY.getKey()));

        // Inicio de la logica de calculo

        // Convertir la entrada del request a la unidad base (HORAS)
        BigDecimal horasReales = this.convertirUnidadAHoras(horasTrabajadasRequest.getCantidad(),
                horasTrabajadasRequest.getUnidad());

        ModeloDePago estrategiaDePago = empleado.getModeloDePago();

        BigDecimal costoPorHoraReal = estrategiaDePago.calcularCostoBasePorHora(empleado.getCostoPorHora());

        HorasTrabajadas horasTrabajadas = horasTrabajadasMapper.toHorasTrabajadas(horasTrabajadasRequest);

        horasTrabajadas.setEmpresa(currentUser.getEmpresa());
        horasTrabajadas.setEmpleado(empleado);
        horasTrabajadas.setProyecto(proyecto);

        // Asignacion de valores calculados y de snapshot
        horasTrabajadas.setHoras(horasReales);
        horasTrabajadas.setCostoPorHoraActual(costoPorHoraReal);
        horasTrabajadas.setCantidad(horasTrabajadasRequest.getCantidad());
        horasTrabajadas.setUnidad(horasTrabajadasRequest.getUnidad());
        horasTrabajadas.setNombreEmpleado(empleado.getNombreCompleto());
        horasTrabajadas.setNombreProyecto(proyecto.getNombreProyecto());

        HorasTrabajadas savedHorasTrabajadas = horasTrabajadasRepository.save(horasTrabajadas);

        actualizarCostoManoDeObraProyecto(proyecto);

        return horasTrabajadasMapper.toHorasTrabajadasResponse(savedHorasTrabajadas);
    }

    @Transactional
    public HorasTrabajadasResponse updateHorasTrabajadas(Long id, HorasTrabajadasRequest horasTrabajadasRequest) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        HorasTrabajadas horasTrabajadas = horasTrabajadasRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCatalog.WORK_LOG_NOT_FOUND.getKey()));

        // --- LÓGICA DE SEGURIDAD ---
        boolean canUpdateAll = hasPermission(currentUser, PERMISO_UPDATE_ALL);

        Long empleadoIdParaActualizar;

        if (canUpdateAll) {
            empleadoIdParaActualizar = horasTrabajadasRequest.getIdEmpleado();
        } else {
            // Si no es admin, solo puede editar sus propios registros
            Empleado empleadoVinculado = getEmpleadoVinculado(currentUser);
            if (!horasTrabajadas.getEmpleado().getId().equals(empleadoVinculado.getId())) {
                throw new ResourceNotFoundException(ErrorCatalog.WORK_LOG_NOT_FOUND.getKey()); // Lanza error
            }
            empleadoIdParaActualizar = empleadoVinculado.getId();
        }

        Proyecto proyectoOriginal = horasTrabajadas.getProyecto();

        Empleado nuevoEmpleado = empleadoRepository
                .findByIdAndEmpresaId(empleadoIdParaActualizar, empresaId)
                .orElseThrow(() -> new BusinessRuleException(
                        ErrorCatalog.INVALID_EMPLOYEE_FOR_COMPANY.getKey()));

        Proyecto nuevoProyecto = proyectoRepository
                .findByIdAndEmpresaId(horasTrabajadasRequest.getIdProyecto(), empresaId)
                .orElseThrow(() -> new BusinessRuleException(
                        ErrorCatalog.INVALID_PROJECT_FOR_COMPANY.getKey()));

        // Logica de calculo de costo/hora
        BigDecimal horasReales = this.convertirUnidadAHoras(horasTrabajadasRequest.getCantidad(),
                horasTrabajadasRequest.getUnidad());
        ModeloDePago estrategiaDePago = nuevoEmpleado.getModeloDePago();
        BigDecimal costoPorHoraReal = estrategiaDePago.calcularCostoBasePorHora(nuevoEmpleado.getCostoPorHora());

        // Mapear campos del request a horas trabajadas
        horasTrabajadasMapper.updateHorasTrabajadasFromRequest(horasTrabajadasRequest, horasTrabajadas);

        // Actualizar relaciones y Snapshot
        horasTrabajadas.setEmpleado(nuevoEmpleado);
        horasTrabajadas.setProyecto(nuevoProyecto);
        horasTrabajadas.setHoras(horasReales);
        horasTrabajadas.setCostoPorHoraActual(costoPorHoraReal);
        horasTrabajadas.setCantidad(horasTrabajadasRequest.getCantidad());
        horasTrabajadas.setUnidad(horasTrabajadasRequest.getUnidad());
        horasTrabajadas.setNombreEmpleado(nuevoEmpleado.getNombreCompleto());
        horasTrabajadas.setNombreProyecto(nuevoProyecto.getNombreProyecto());

        HorasTrabajadas updatedHorasTrabajadas = horasTrabajadasRepository.save(horasTrabajadas);

        actualizarCostoManoDeObraProyecto(proyectoOriginal);
        if (!proyectoOriginal.getId().equals(updatedHorasTrabajadas.getProyecto().getId())) {
            actualizarCostoManoDeObraProyecto(updatedHorasTrabajadas.getProyecto());
        }

        return horasTrabajadasMapper.toHorasTrabajadasResponse(updatedHorasTrabajadas);
    }

    @Transactional
    public void deleteHorasTrabajadas(Long id) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        HorasTrabajadas horasTrabajadas = horasTrabajadasRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCatalog.WORK_LOG_NOT_FOUND.getKey()));

        // --- LÓGICA DE SEGURIDAD ---
        boolean canDeleteAll = hasPermission(currentUser, PERMISO_DELETE_ALL);

        if (!canDeleteAll) {
            Empleado empleadoVinculado = getEmpleadoVinculado(currentUser);
            if (!horasTrabajadas.getEmpleado().getId().equals(empleadoVinculado.getId())) {
                throw new ResourceNotFoundException(ErrorCatalog.WORK_LOG_NOT_FOUND.getKey()); // Lanza error
            }
        }

        Proyecto proyectoAfectado = horasTrabajadas.getProyecto();
        horasTrabajadasRepository.delete(horasTrabajadas);

        if (proyectoAfectado != null) {
            actualizarCostoManoDeObraProyecto(proyectoAfectado);
        }
    }

    private void actualizarCostoManoDeObraProyecto(Proyecto proyecto) {
        // Llama a la consulta para calcular el costo total
        BigDecimal nuevoCostoTotal = horasTrabajadasRepository
                .sumCostoManoDeObraByProyectoId(proyecto.getId(), proyecto.getEmpresa().getId());

        // Maneja el caso de que no haya registros (SUM puede devoler Null)
        proyecto.setCostoManoDeObra(nuevoCostoTotal != null ? nuevoCostoTotal : BigDecimal.ZERO);
        proyectoRepository.save(proyecto);
    }

    @Transactional(readOnly = true)
    public List<HorasTrabajadasExportDTO> findHorasTrabajadasForExport(String filter, String sort) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Sort sortObj = Sort.by(Sort.Direction.DESC, "fecha"); // Ordenar por fecha por defecto
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String property = sortParts[0];
                Sort.Direction direction = "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                sortObj = Sort.by(direction, property);
            }
        }

        String effectiveFilter = (filter != null && !filter.trim().isEmpty()) ? filter.trim().toLowerCase() : null;
        List<HorasTrabajadas> horas = horasTrabajadasRepository.findByFilterForExport(empresaId, effectiveFilter,
                sortObj);

        return horas.stream()
                .map(HorasTrabajadasExportDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Método helper privado para centralizar la conversión de la ENTRADA
     * (Request) a la unidad base (horas).
     */
    private BigDecimal convertirUnidadAHoras(BigDecimal cantidad, String unidad) {
        if ("dias".equalsIgnoreCase(unidad)) {
            // Esta es tu fuente de verdad para la jornada laboral
            return cantidad.multiply(new BigDecimal("8"));
        }
        if ("horas".equalsIgnoreCase(unidad)) {
            return cantidad;
        }
        // Así escalas si el frontend envía "semanas"
        // if ("semanas".equalsIgnoreCase(unidad)) {
        // return cantidad.multiply(new BigDecimal("40"));
        // }

        // Lanza una excepción si la unidad no se reconoce
        throw new BusinessRuleException(ErrorCatalog.INVALID_INPUT_UNIT.getKey());
    }

    /**
     * Método helper privado para obtener el empleado vinculado a un usuario
     * y manejar la excepción si no existe.
     */
    private Empleado getEmpleadoVinculado(User currentUser) {
        Empleado empleadoVinculado = currentUser.getEmpleado();
        if (empleadoVinculado == null) {

            throw new BusinessRuleException(ErrorCatalog.USER_NOT_LINKED_TO_EMPLOYEE.getKey());
        }
        return empleadoVinculado;
    }

    /**
     * Método helper privado para comprobar permisos usando el modelo de dominio.
     */
    private boolean hasPermission(User user, String permissionName) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        // Itera a través de los roles, luego los permisos de cada rol
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

}
