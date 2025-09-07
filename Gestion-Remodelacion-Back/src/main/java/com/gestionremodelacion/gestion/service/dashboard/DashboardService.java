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
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.proyecto.repository.ProyectoRepository;
import com.gestionremodelacion.gestion.service.user.UserService;

@Service
public class DashboardService {

    private final ProyectoRepository proyectoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final HorasTrabajadasRepository horasTrabajadasRepository;
    private final ClienteRepository clienteRepository;
    private final UserService userService;

    public DashboardService(ProyectoRepository proyectoRepository, EmpleadoRepository empleadoRepository,
            HorasTrabajadasRepository horasTrabajadasRepository, ClienteRepository clienteRepository,
            UserService userService) {
        this.proyectoRepository = proyectoRepository;
        this.empleadoRepository = empleadoRepository;
        this.horasTrabajadasRepository = horasTrabajadasRepository;
        this.clienteRepository = clienteRepository;
        this.userService = userService;
    }

    // Método para obtener proyectos por año y mes
    @Transactional(readOnly = true)
    public List<Object[]> getProyectos(Integer year, Integer month) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        return proyectoRepository.findProyectosByYearAndMonth(empresaId, year, month);
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(Integer year, Integer month, Long projectId) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

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
            totalProyectos = 1L;
            montoRecibido = Optional.ofNullable(proyectoRepository.sumMontoRecibidoByProjectId(projectId, empresaId))
                    .orElse(BigDecimal.ZERO);
            costoMateriales = Optional
                    .ofNullable(proyectoRepository.sumCostoMaterialesConsolidadoByProjectId(projectId, empresaId))
                    .orElse(BigDecimal.ZERO);
            otrosGastos = Optional
                    .ofNullable(proyectoRepository.sumOtrosGastosDirectosConsolidadoByProjectId(projectId, empresaId))
                    .orElse(BigDecimal.ZERO);
            costoManoDeObra = Optional
                    .ofNullable(proyectoRepository.sumCostoManoDeObraByProjectId(projectId, empresaId))
                    .orElse(BigDecimal.ZERO);
            proyectosPorEstado = proyectoRepository.countProyectosByEstadoByProjectId(projectId, empresaId);
            horasPorProyecto = horasTrabajadasRepository.sumHorasByProyectoByProjectId(empresaId, projectId);
            horasPorEmpleadoProyecto = horasTrabajadasRepository.sumHorasByEmpleadoAndProyectoByProjectId(empresaId,
                    projectId);
            empleadosPorRol = horasTrabajadasRepository.countEmpleadosByRolByProjectId(empresaId, projectId);
        } else if (month != null && month > 0) {
            totalProyectos = proyectoRepository.countByYearAndMonth(empresaId, targetYear, month);
            montoRecibido = Optional
                    .ofNullable(proyectoRepository.sumMontoRecibidoByYearAndMonth(empresaId, targetYear, month))
                    .orElse(BigDecimal.ZERO);
            costoMateriales = Optional.ofNullable(
                    proyectoRepository.sumCostoMaterialesConsolidadoByYearAndMonth(empresaId, targetYear, month))
                    .orElse(BigDecimal.ZERO);
            otrosGastos = Optional.ofNullable(
                    proyectoRepository.sumOtrosGastosDirectosConsolidadoByYearAndMonth(empresaId, targetYear, month))
                    .orElse(BigDecimal.ZERO);
            costoManoDeObra = Optional
                    .ofNullable(proyectoRepository.sumCostoManoDeObraByYearAndMonth(empresaId, targetYear, month))
                    .orElse(BigDecimal.ZERO);
            proyectosPorEstado = proyectoRepository.countProyectosByEstadoByYearAndMonth(empresaId, targetYear, month);
            horasPorProyecto = horasTrabajadasRepository.sumHorasByProyectoByYearAndMonth(empresaId, targetYear, month);
            horasPorEmpleadoProyecto = horasTrabajadasRepository.sumHorasByEmpleadoAndProyectoByYearAndMonth(empresaId,
                    targetYear, month);
            empleadosPorRol = horasTrabajadasRepository.countEmpleadosByRolByYearAndMonth(empresaId, targetYear, month);
        } else {
            totalProyectos = proyectoRepository.countByYear(empresaId, targetYear);
            montoRecibido = Optional.ofNullable(proyectoRepository.sumMontoRecibidoByYear(empresaId, targetYear))
                    .orElse(BigDecimal.ZERO);
            costoMateriales = Optional
                    .ofNullable(proyectoRepository.sumCostoMaterialesConsolidadoByYear(empresaId, targetYear))
                    .orElse(BigDecimal.ZERO);
            otrosGastos = Optional
                    .ofNullable(proyectoRepository.sumOtrosGastosDirectosConsolidadoByYear(empresaId, targetYear))
                    .orElse(BigDecimal.ZERO);
            costoManoDeObra = Optional.ofNullable(proyectoRepository.sumCostoManoDeObraByYear(empresaId, targetYear))
                    .orElse(BigDecimal.ZERO);
            proyectosPorEstado = proyectoRepository.countProyectosByEstadoByYear(empresaId, targetYear);
            horasPorProyecto = horasTrabajadasRepository.sumHorasByProyectoByYear(empresaId, targetYear);
            horasPorEmpleadoProyecto = horasTrabajadasRepository.sumHorasByEmpleadoAndProyectoByYear(empresaId,
                    targetYear);
            empleadosPorRol = horasTrabajadasRepository.countEmpleadosByRolByYear(empresaId, targetYear);
        }

        // --- Métricas que no se filtran o tienen su propio filtro ---
        BigDecimal balanceFinanciero = montoRecibido.subtract(costoMateriales).subtract(otrosGastos)
                .subtract(costoManoDeObra);
        Long empleadosActivos = empleadoRepository.countByEmpresaIdAndActivo(empresaId);

        return new DashboardSummaryResponse(
                totalProyectos, empleadosActivos, balanceFinanciero, montoRecibido, costoMateriales, otrosGastos,
                empleadosPorRol, horasPorProyecto, proyectosPorEstado,
                horasPorEmpleadoProyecto, costoManoDeObra);
    }

    @Transactional(readOnly = true)
    public DashboardClientesResponse getDashboardClientesSummary(Integer year, Integer month) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        // Para este gráfico, siempre queremos los 12 meses del año seleccionado
        List<Object[]> clientesPorMes = clienteRepository.countClientesByMonthForYearAndEmpresa(empresaId, targetYear);

        return new DashboardClientesResponse(clientesPorMes);
    }

    // Nuevo método para obtener solo la lista de años.
    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears() {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        return proyectoRepository.findDistinctYearsByEmpresaId(empresaId);
    }
}
