package com.gestionremodelacion.gestion.service.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.cliente.repository.ClienteRepository;
import com.gestionremodelacion.gestion.dto.response.DashboardClientesResponse;
import com.gestionremodelacion.gestion.dto.response.DashboardSummaryResponse;
import com.gestionremodelacion.gestion.empleado.repository.EmpleadoRepository;
import com.gestionremodelacion.gestion.horastrabajadas.repository.HorasTrabajadasRepository;
import com.gestionremodelacion.gestion.proyecto.repository.ProyectoRepository;

@Service
public class DashboardService {

    private final ProyectoRepository proyectoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final HorasTrabajadasRepository horasTrabajadasRepository;
    private final ClienteRepository clienteRepository;

    public DashboardService(ProyectoRepository proyectoRepository, EmpleadoRepository empleadoRepository, HorasTrabajadasRepository horasTrabajadasRepository, ClienteRepository clienteRepository) {
        this.proyectoRepository = proyectoRepository;
        this.empleadoRepository = empleadoRepository;
        this.horasTrabajadasRepository = horasTrabajadasRepository;
        this.clienteRepository = clienteRepository;
    }

    // Método para obtener proyectos por año y mes
    @Transactional(readOnly = true)
    public List<Object[]> getProyectos(Integer year, Integer month) {
        return proyectoRepository.findProyectosByYearAndMonth(year, month);
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(Integer year, Integer month, Long projectId) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        // Declaramos variables
        Long totalProyectos;
        BigDecimal montoRecibido;
        BigDecimal costoMateriales;
        BigDecimal otrosGastos;
        BigDecimal costoManoDeObra;
        List<Object[]> proyectosPorEstado;
        List<Object[]> horasPorProyecto;
        List<Object[]> horasPorEmpleadoProyecto;
        List<Object[]> empleadosPorRol;

        if (projectId != null) {
            // Lógica para filtrar por proyecto
            totalProyectos = 1L; // Solo hay 1 proyecto si se filtra por ID
            montoRecibido = Optional.ofNullable(proyectoRepository.sumMontoRecibidoByProjectId(projectId)).orElse(BigDecimal.ZERO);
            costoMateriales = Optional.ofNullable(proyectoRepository.sumCostoMaterialesConsolidadoByProjectId(projectId)).orElse(BigDecimal.ZERO);
            otrosGastos = Optional.ofNullable(proyectoRepository.sumOtrosGastosDirectosConsolidadoByProjectId(projectId)).orElse(BigDecimal.ZERO);
            costoManoDeObra = Optional.ofNullable(proyectoRepository.sumCostoManoDeObraByProjectId(projectId)).orElse(BigDecimal.ZERO);
            proyectosPorEstado = proyectoRepository.countProyectosByEstadoByProjectId(projectId);
            horasPorProyecto = horasTrabajadasRepository.sumHorasByProyectoByProjectId(projectId);
            horasPorEmpleadoProyecto = horasTrabajadasRepository.sumHorasByEmpleadoAndProyectoByProjectId(projectId);
            empleadosPorRol = horasTrabajadasRepository.countEmpleadosByRolByProjectId(projectId);

        } else if (month != null && month > 0) {
            totalProyectos = proyectoRepository.countByYearAndMonth(targetYear, month);
            montoRecibido = Optional.ofNullable(proyectoRepository.sumMontoRecibidoByYearAndMonth(targetYear, month)).orElse(BigDecimal.ZERO);
            costoMateriales = Optional.ofNullable(proyectoRepository.sumCostoMaterialesConsolidadoByYearAndMonth(targetYear, month)).orElse(BigDecimal.ZERO);
            otrosGastos = Optional.ofNullable(proyectoRepository.sumOtrosGastosDirectosConsolidadoByYearAndMonth(targetYear, month)).orElse(BigDecimal.ZERO);
            costoManoDeObra = Optional.ofNullable(proyectoRepository.sumCostoManoDeObraByYearAndMonth(targetYear, month)).orElse(BigDecimal.ZERO);
            proyectosPorEstado = proyectoRepository.countProyectosByEstadoByYearAndMonth(targetYear, month);
            horasPorProyecto = horasTrabajadasRepository.sumHorasByProyectoByYearAndMonth(targetYear, month);
            horasPorEmpleadoProyecto = horasTrabajadasRepository.sumHorasByEmpleadoAndProyectoByYearAndMonth(targetYear, month);
            empleadosPorRol = horasTrabajadasRepository.countEmpleadosByRolByYearAndMonth(targetYear, month);
        } else {
            // Si no hay mes, filtramos solo por año
            totalProyectos = proyectoRepository.countByYear(targetYear);
            montoRecibido = Optional.ofNullable(proyectoRepository.sumMontoRecibidoByYear(targetYear)).orElse(BigDecimal.ZERO);
            costoMateriales = Optional.ofNullable(proyectoRepository.sumCostoMaterialesConsolidadoByYear(targetYear)).orElse(BigDecimal.ZERO);
            otrosGastos = Optional.ofNullable(proyectoRepository.sumOtrosGastosDirectosConsolidadoByYear(targetYear)).orElse(BigDecimal.ZERO);
            costoManoDeObra = Optional.ofNullable(proyectoRepository.sumCostoManoDeObraByYear(targetYear)).orElse(BigDecimal.ZERO);
            proyectosPorEstado = proyectoRepository.countProyectosByEstadoByYear(targetYear);
            horasPorProyecto = horasTrabajadasRepository.sumHorasByProyectoByYear(targetYear);
            horasPorEmpleadoProyecto = horasTrabajadasRepository.sumHorasByEmpleadoAndProyectoByYear(targetYear);
            empleadosPorRol = horasTrabajadasRepository.countEmpleadosByRolByYear(targetYear);
        }

        // --- Métricas que no se filtran o tienen su propio filtro ---
        BigDecimal balanceFinanciero = montoRecibido.subtract(costoMateriales).subtract(otrosGastos).subtract(costoManoDeObra);
        Long empleadosActivos = empleadoRepository.countByActivo(true);
        

        return new DashboardSummaryResponse(
                totalProyectos, empleadosActivos, balanceFinanciero, montoRecibido, costoMateriales, otrosGastos,
                empleadosPorRol, horasPorProyecto, proyectosPorEstado,
                horasPorEmpleadoProyecto, costoManoDeObra
        );
    }

    @Transactional(readOnly = true)
    public DashboardClientesResponse getDashboardClientesSummary(Integer year, Integer month) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        // Para este gráfico, siempre queremos los 12 meses del año seleccionado
        List<Object[]> clientesPorMes = clienteRepository.countClientesByMonthForYear(targetYear);

        return new DashboardClientesResponse(clientesPorMes);
    }

    // Nuevo método para obtener solo la lista de años.
    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears() {
        return proyectoRepository.findDistinctYears();
    }
}
