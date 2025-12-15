package com.gestionremodelacion.gestion.service.dashboard;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.cliente.repository.ClienteRepository;
import com.gestionremodelacion.gestion.dto.response.DashboardClientesResponse;
import com.gestionremodelacion.gestion.dto.response.DashboardSummaryResponse;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.proyecto.repository.ProyectoRepository;
import com.gestionremodelacion.gestion.service.dashboard.metrics.FinanceMetricService;
import com.gestionremodelacion.gestion.service.dashboard.metrics.HumanResourcesMetricService;
import com.gestionremodelacion.gestion.service.dashboard.metrics.ProjectMetricService;
import com.gestionremodelacion.gestion.service.user.UserService;

@Service
public class DashboardService {

    private final ProyectoRepository proyectoRepository;
    private final ClienteRepository clienteRepository;
    private final UserService userService;
    private final ProjectMetricService projectMetricService;
    private final FinanceMetricService financeMetricService;
    private final HumanResourcesMetricService humanResourcesMetricService;

    public DashboardService(
            ProyectoRepository proyectoRepository,
            ClienteRepository clienteRepository,
            UserService userService,
            ProjectMetricService projectMetricService,
            FinanceMetricService financeMetricService,
            HumanResourcesMetricService humanResourcesMetricService) {
        this.proyectoRepository = proyectoRepository;
        this.clienteRepository = clienteRepository;
        this.userService = userService;
        this.projectMetricService = projectMetricService;
        this.financeMetricService = financeMetricService;
        this.humanResourcesMetricService = humanResourcesMetricService;
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

        // Llamada a servicios metricos
        var projectData = projectMetricService.getProjectStats(empresaId, targetYear, month, projectId);

        // Llama a servicios RRHH
        var hrData = humanResourcesMetricService.getHRStats(empresaId, targetYear, month, projectId);

        var financeData = financeMetricService.calculateFinancials(empresaId, targetYear, month, projectId);

        return new DashboardSummaryResponse(
                projectData.total(), // Total Proyectos
                hrData.empleadosActivos(), // Empleados Activos
                financeData.balance(), // Balance Financiero
                financeData.ingresos(), // Monto Recibido
                financeData.materiales(), // Costo Materiales
                financeData.otros(), // Otros Gastos
                hrData.empleadosPorRol(), // Lista: Empleados por Rol
                hrData.horasPorProyecto(), // Lista: Horas por Proyecto
                projectData.proyectosPorEstado(), // Lista: Proyectos por Estado
                hrData.horasPorEmpleadoProyecto(), // Lista: Horas por Empleado/Proyecto
                financeData.manoObra() // Costo Mano de Obra
        );
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
