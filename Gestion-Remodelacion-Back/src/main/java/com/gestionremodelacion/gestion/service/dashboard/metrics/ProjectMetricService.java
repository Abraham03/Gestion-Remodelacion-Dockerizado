package com.gestionremodelacion.gestion.service.dashboard.metrics;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gestionremodelacion.gestion.proyecto.repository.ProyectoRepository;

@Service
public class ProjectMetricService {

    private final ProyectoRepository proyectoRepository;

    // Constructor manual (Sin Lombok)
    public ProjectMetricService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    public ProjectStatusDTO getProjectStats(Long empresaId, int year, Integer month, Long projectId) {
        long totalProyectos;
        List<Object[]> proyectosPorEstado;

        // 1. NIVEL: FILTRO POR PROYECTO ESPECÍFICO
        if (projectId != null) {
            totalProyectos = 1L;
            // Usamos tu método existente que devuelve List<Object[]>
            proyectosPorEstado = proyectoRepository.countProyectosByEstadoByProjectId(projectId, empresaId);

            // 2. NIVEL: FILTRO POR MES
        } else if (month != null && month > 0) {
            totalProyectos = proyectoRepository.countByYearAndMonth(empresaId, year, month);
            // Usamos tu método existente
            proyectosPorEstado = proyectoRepository.countProyectosByEstadoByYearAndMonth(empresaId, year, month);

            // 3. NIVEL: FILTRO POR AÑO (DEFAULT)
        } else {
            totalProyectos = proyectoRepository.countByYear(empresaId, year);
            // Usamos tu método existente
            proyectosPorEstado = proyectoRepository.countProyectosByEstadoByYear(empresaId, year);
        }

        // Manejo de seguridad: si la lista es nula, devolvemos lista vacía para evitar errores en el DTO
        if (proyectosPorEstado == null) {
            proyectosPorEstado = Collections.emptyList();
        }

        return new ProjectStatusDTO(totalProyectos, proyectosPorEstado);
    }

    // DTO ajustado para devolver List<Object[]> tal como lo espera tu DashboardSummaryResponse
    public record ProjectStatusDTO(long total, List<Object[]> proyectosPorEstado) {

    }
}
