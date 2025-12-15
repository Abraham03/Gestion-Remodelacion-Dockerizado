package com.gestionremodelacion.gestion.service.dashboard.metrics;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gestionremodelacion.gestion.empleado.repository.EmpleadoRepository;
import com.gestionremodelacion.gestion.horastrabajadas.repository.HorasTrabajadasRepository;

@Service
public class HumanResourcesMetricService {

    private final EmpleadoRepository empleadoRepository;
    private final HorasTrabajadasRepository horasTrabajadasRepository;

    public HumanResourcesMetricService(EmpleadoRepository empleadoRepository,
            HorasTrabajadasRepository horasTrabajadasRepository) {
        this.empleadoRepository = empleadoRepository;
        this.horasTrabajadasRepository = horasTrabajadasRepository;
    }

    public HRMetricsDTO getHRStats(Long empresaId, int year, Integer month, Long projectId) {
        // 1. Empleados Activos 
        Long empleadosActivos = empleadoRepository.countByEmpresaIdAndActivo(empresaId);

        List<Object[]> empleadosPorRol;
        List<Object[]> horasPorProyecto;
        List<Object[]> horasPorEmpleadoProyecto;

        // 2. LÓGICA DE FILTRADO 
        // A. Filtro por Proyecto
        if (projectId != null) {
            horasPorProyecto = horasTrabajadasRepository.sumHorasByProyectoByProjectId(empresaId, projectId);
            horasPorEmpleadoProyecto = horasTrabajadasRepository.sumHorasByEmpleadoAndProyectoByProjectId(empresaId, projectId);
            empleadosPorRol = horasTrabajadasRepository.countEmpleadosByRolByProjectId(empresaId, projectId);

            // B. Filtro por Mes
        } else if (month != null && month > 0) {
            horasPorProyecto = horasTrabajadasRepository.sumHorasByProyectoByYearAndMonth(empresaId, year, month);
            horasPorEmpleadoProyecto = horasTrabajadasRepository.sumHorasByEmpleadoAndProyectoByYearAndMonth(empresaId, year, month);
            empleadosPorRol = horasTrabajadasRepository.countEmpleadosByRolByYearAndMonth(empresaId, year, month);

            // C. Filtro por Año (Default)
        } else {
            horasPorProyecto = horasTrabajadasRepository.sumHorasByProyectoByYear(empresaId, year);
            horasPorEmpleadoProyecto = horasTrabajadasRepository.sumHorasByEmpleadoAndProyectoByYear(empresaId, year);
            empleadosPorRol = horasTrabajadasRepository.countEmpleadosByRolByYear(empresaId, year);
        }

        // Seguridad para evitar nulls en las listas
        if (horasPorProyecto == null) {
            horasPorProyecto = Collections.emptyList();
        }
        if (horasPorEmpleadoProyecto == null) {
            horasPorEmpleadoProyecto = Collections.emptyList();
        }
        if (empleadosPorRol == null) {
            empleadosPorRol = Collections.emptyList();
        }

        return new HRMetricsDTO(empleadosActivos, empleadosPorRol, horasPorProyecto, horasPorEmpleadoProyecto);
    }

    // DTO Interno que mantiene la estructura List<Object[]> para no romper el Frontend
    public record HRMetricsDTO(
            Long empleadosActivos,
            List<Object[]> empleadosPorRol,
            List<Object[]> horasPorProyecto,
            List<Object[]> horasPorEmpleadoProyecto
            ) {

    }
}
