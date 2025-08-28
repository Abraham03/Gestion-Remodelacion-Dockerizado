package com.gestionremodelacion.gestion.controller.dashboard;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.dto.response.DashboardClientesResponse;
import com.gestionremodelacion.gestion.dto.response.DashboardSummaryResponse;
import com.gestionremodelacion.gestion.service.dashboard.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(
        @RequestParam(name = "year", required = false) Integer year, 
        @RequestParam(name = "month", required = false) Integer month,
        @RequestParam(name = "projectId", required = false) Long projectId) {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(year, month, projectId);

        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/proyectos")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getProyectos(
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month) {
        List<Object[]> proyectos = dashboardService.getProyectos(year, month);
        return ResponseEntity.ok(ApiResponse.success(proyectos));
    }    

    @GetMapping("/clientes-summary")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<DashboardClientesResponse>> getClientesSummary(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month) {

        DashboardClientesResponse summary = dashboardService.getDashboardClientesSummary(year, month);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // Endpoint para obtener solo los años.
    @GetMapping("/years")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<List<Integer>>> getAvailableYears() {
        List<Integer> years = dashboardService.getAvailableYears();
        return ResponseEntity.ok(ApiResponse.success(years));
    }
}
