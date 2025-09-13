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

        return (filter != null && !filter.trim().isEmpty())
                ? horasTrabajadasRepository.findByFilterWithDetails(empresaId, filter, pageable)
                : horasTrabajadasRepository.findAllWithDetails(empresaId, pageable);
    }

    @Transactional(readOnly = true)
    public HorasTrabajadasResponse getHorasTrabajadasById(Long id) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        return horasTrabajadasRepository.findByIdAndEmpresaId(id, empresaId)
                .map(horasTrabajadasMapper::toHorasTrabajadasResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.RESOURCE_NOT_FOUND.getKey()));
    }

    @Transactional
    public HorasTrabajadasResponse createHorasTrabajadas(HorasTrabajadasRequest horasTrabajadasRequest) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Empleado empleado = empleadoRepository.findByIdAndEmpresaId(horasTrabajadasRequest.getIdEmpleado(), empresaId)
                .orElseThrow(() -> new BusinessRuleException(
                        ErrorCatalog.INVALID_EMPLOYEE_FOR_COMPANY.getKey()));

        Proyecto proyecto = proyectoRepository.findByIdAndEmpresaId(horasTrabajadasRequest.getIdProyecto(), empresaId)
                .orElseThrow(() -> new BusinessRuleException(
                        ErrorCatalog.INVALID_PROJECT_FOR_COMPANY.getKey()));

        HorasTrabajadas horasTrabajadas = horasTrabajadasMapper.toHorasTrabajadas(horasTrabajadasRequest);
        horasTrabajadas.setEmpresa(currentUser.getEmpresa());
        horasTrabajadas.setEmpleado(empleado);
        horasTrabajadas.setProyecto(proyecto);
        horasTrabajadas.setCostoPorHoraActual(empleado.getCostoPorHora());

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

        Proyecto proyectoOriginal = horasTrabajadas.getProyecto();

        Empleado nuevoEmpleado = empleadoRepository
                .findByIdAndEmpresaId(horasTrabajadasRequest.getIdEmpleado(), empresaId)
                .orElseThrow(() -> new BusinessRuleException(
                        ErrorCatalog.INVALID_EMPLOYEE_FOR_COMPANY.getKey()));

        Proyecto nuevoProyecto = proyectoRepository
                .findByIdAndEmpresaId(horasTrabajadasRequest.getIdProyecto(), empresaId)
                .orElseThrow(() -> new BusinessRuleException(
                        ErrorCatalog.INVALID_PROJECT_FOR_COMPANY.getKey()));

        horasTrabajadasMapper.updateHorasTrabajadasFromRequest(horasTrabajadasRequest, horasTrabajadas);
        horasTrabajadas.setEmpleado(nuevoEmpleado);
        horasTrabajadas.setProyecto(nuevoProyecto);
        horasTrabajadas.setCostoPorHoraActual(nuevoEmpleado.getCostoPorHora());

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

        Proyecto proyectoAfectado = horasTrabajadas.getProyecto();
        horasTrabajadasRepository.delete(horasTrabajadas);

        if (proyectoAfectado != null) {
            actualizarCostoManoDeObraProyecto(proyectoAfectado);
        }
    }

    private void actualizarCostoManoDeObraProyecto(Proyecto proyecto) {
        List<HorasTrabajadas> horasDelProyecto = horasTrabajadasRepository
                .findByProyectoIdAndEmpresaId(proyecto.getId(), proyecto.getEmpresa().getId());
        BigDecimal nuevoCostoTotal = horasDelProyecto.stream()
                .map(h -> h.getHoras().multiply(h.getCostoPorHoraActual()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        proyecto.setCostoManoDeObra(nuevoCostoTotal);
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
     * ✅ MÉTODO DE MIGRACIÓN 1: Corrige los registros históricos.
     * Itera sobre todos los registros de horas trabajadas con 'costoPorHoraActual'
     * nulo,
     * asigna el costo por hora actual del empleado y guarda el registro.
     * Nota: Este método solo debe ejecutarse una vez en el despliegue.
     * 
     * @Transactional
     *                public void corregirCostosHorasHistoricas() {
     *                System.out.println("Iniciando corrección de costos por hora en
     *                registros históricos...");
     * 
     *                // Usamos una consulta personalizada para obtener solo los
     *                registros que
     *                // necesitan ser corregidos
     *                List<HorasTrabajadas> registrosSinCosto =
     *                horasTrabajadasRepository.findByCostoPorHoraActualIsNull();
     * 
     *                for (HorasTrabajadas registro : registrosSinCosto) {
     *                // Se asume que la entidad Empleado ya está cargada debido a
     *                las anotaciones de
     *                // relación
     *                Empleado empleado = registro.getEmpleado();
     *                if (empleado != null && empleado.getCostoPorHora() != null) {
     *                registro.setCostoPorHoraActual(empleado.getCostoPorHora());
     *                horasTrabajadasRepository.save(registro);
     *                }
     *                }
     *                System.out.println(
     *                "Corrección de costos por hora completada. Registros
     *                actualizados: " + registrosSinCosto.size());
     *                }
     * 
     *                /**
     *                ✅ MÉTODO DE MIGRACIÓN 2: Recalcula los gastos consolidados de
     *                los proyectos.
     *                Este método se ejecuta DESPUÉS de corregir los costos de
     *                horas.
     *                Recalcula el campo 'otrosGastosDirectosConsolidado' en cada
     *                proyecto
     *                sumando todos los costos de horas trabajadas asociados.
     *                Nota: Este método solo debe ejecutarse una vez en el
     *                despliegue.
     */
    @Transactional
    public void recalcularGastosConsolidadosProyectos() {
        System.out.println("Iniciando recálculo de gastos consolidados en proyectos...");

        // 1. Obtener todos los proyectos
        List<Proyecto> todosLosProyectos = proyectoRepository.findAll();

        // 2. Reiniciar el campo de gastos en cada proyecto para evitar duplicados
        for (Proyecto proyecto : todosLosProyectos) {
            proyecto.setCostoManoDeObra(BigDecimal.ZERO);
            proyecto.setOtrosGastosDirectosConsolidado(BigDecimal.ZERO);
            proyectoRepository.save(proyecto);
        }

        // 3. Obtener todos los registros de horas trabajadas (ya corregidos)
        List<HorasTrabajadas> todasLasHoras = horasTrabajadasRepository.findAll();

        // 4. Sumar los costos y actualizar cada proyecto
        for (HorasTrabajadas registro : todasLasHoras) {
            if (registro.getProyecto() != null && registro.getCostoPorHoraActual() != null) {
                Proyecto proyecto = registro.getProyecto();
                BigDecimal costoTotal = registro.getHoras().multiply(registro.getCostoPorHoraActual());

                proyecto.setCostoManoDeObra(proyecto.getCostoManoDeObra().add(costoTotal));
                proyectoRepository.save(proyecto);
            }
        }
        System.out.println("Recálculo de gastos consolidados completado.");
    }

}
