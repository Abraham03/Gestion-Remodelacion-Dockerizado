package com.gestionremodelacion.gestion.proyecto.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.empresa.model.Empresa.PlanSuscripcion;
import com.gestionremodelacion.gestion.export.ExporterService;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.proyecto.dto.request.ProyectoRequest;
import com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoExcelDTO;
import com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoPdfDTO;
import com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoResponse;
import com.gestionremodelacion.gestion.proyecto.service.ProyectoService;
import com.gestionremodelacion.gestion.security.annotations.RequiresPlan;
import com.gestionremodelacion.gestion.service.user.UserService;
import com.itextpdf.text.DocumentException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    private final ProyectoService proyectoService;
    private final ExporterService exporterService;
    private final UserService userService;

    public ProyectoController(ProyectoService proyectoService, ExporterService exporterService,
            UserService userService) {
        this.proyectoService = proyectoService;
        this.exporterService = exporterService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROYECTO_READ')")
    public ResponseEntity<ApiResponse<Page<ProyectoResponse>>> getAllProyectos(
            Pageable pageable,
            @RequestParam(name = "filter", required = false) String filter) {
        Page<ProyectoResponse> proyectosPage = proyectoService.getAllProyectos(pageable, filter);
        return ResponseEntity
                .ok(new ApiResponse<>(HttpStatus.OK.value(), "Proyectos obtenidos con éxito", proyectosPage));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROYECTO_READ')")
    public ResponseEntity<ApiResponse<ProyectoResponse>> getProyectoById(@PathVariable Long id) {
        ProyectoResponse proyecto = proyectoService.getProyectoById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Proyecto obtenido con éxito", proyecto));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROYECTO_CREATE')")
    public ResponseEntity<ApiResponse<ProyectoResponse>> createProyecto(
            @Valid @RequestBody ProyectoRequest proyectoRequest) {
        ProyectoResponse createdProyecto = proyectoService.createProyecto(proyectoRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Proyecto creado con éxito", createdProyecto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROYECTO_UPDATE')")
    public ResponseEntity<ApiResponse<ProyectoResponse>> updateProyecto(@PathVariable Long id,
            @Valid @RequestBody ProyectoRequest proyectoRequest) {
        ProyectoResponse updatedProyecto = proyectoService.updateProyecto(id, proyectoRequest);
        return ResponseEntity
                .ok(new ApiResponse<>(HttpStatus.OK.value(), "Proyecto actualizado con éxito", updatedProyecto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROYECTO_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteProyecto(@PathVariable Long id) {
        proyectoService.deleteProyecto(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Proyecto eliminado con éxito", null));
    }

    /**
     * Endpoint para exportar a Excel.
     */
    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('PROYECTO_READ')")
    @RequiresPlan({ PlanSuscripcion.NEGOCIOS, PlanSuscripcion.PROFESIONAL })
    public ResponseEntity<byte[]> exportProyectosToExcel(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws IOException {

        User currentUser = userService.getCurrentUser();
        Empresa empresa = currentUser.getEmpresa();

        List<ProyectoExcelDTO> proyectos = proyectoService.findProyectosForExcelExport(filter, sort);
        ByteArrayOutputStream excelStream = exporterService.exportToExcel(proyectos, "Reporte de Proyectos", empresa);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Proyectos.xlsx");
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return ResponseEntity.ok().headers(headers).body(excelStream.toByteArray());
    }

    /**
     * Endpoint para exportar a PDF.
     */
    @GetMapping("/export/pdf")
    @PreAuthorize("hasAuthority('PROYECTO_READ')")
    @RequiresPlan({ PlanSuscripcion.NEGOCIOS, PlanSuscripcion.PROFESIONAL })
    public ResponseEntity<byte[]> exportProyectosToPdf(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws DocumentException, IOException {

        User currentUser = userService.getCurrentUser();
        Empresa empresa = currentUser.getEmpresa();

        List<ProyectoPdfDTO> proyectos = proyectoService.findProyectosForPdfExport(filter, sort);
        if (proyectos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        ByteArrayOutputStream pdfStream = exporterService.exportToPdf(proyectos, "Reporte de Proyectos", empresa);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Proyectos.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok().headers(headers).body(pdfStream.toByteArray());
    }
}
