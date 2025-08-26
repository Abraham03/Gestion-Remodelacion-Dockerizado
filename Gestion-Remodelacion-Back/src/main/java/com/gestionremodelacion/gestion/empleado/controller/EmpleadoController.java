package com.gestionremodelacion.gestion.empleado.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // Importar Page
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus; // Importar Pageable
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity; // Importar PageableDefault para valores por defecto
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.empleado.dto.request.EmpleadoRequest;
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoExportDTO;
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoResponse;
import com.gestionremodelacion.gestion.empleado.service.EmpleadoService;
import com.gestionremodelacion.gestion.export.ExporterService;
import com.itextpdf.text.DocumentException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final ExporterService exporterService;

    public EmpleadoController(EmpleadoService empleadoService, ExporterService exporterService) {
        this.empleadoService = empleadoService;
        this.exporterService = exporterService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLEADO_READ')")
    public ResponseEntity<ApiResponse<Page<EmpleadoResponse>>> getAllEmpleados(
            Pageable pageable,
            @RequestParam(name = "filter", required = false) String filter) {
        Page<EmpleadoResponse> empleadosPage = empleadoService.getAllEmpleados(pageable, filter);
        return ResponseEntity.ok(ApiResponse.success(empleadosPage));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLEADO_READ')")
    public ResponseEntity<EmpleadoResponse> getEmpleadoById(@PathVariable Long id) {
        EmpleadoResponse empleado = empleadoService.getEmpleadoById(id);
        return ResponseEntity.ok(empleado);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLEADO_CREATE')")
    public ResponseEntity<EmpleadoResponse> createEmpleado(@Valid @RequestBody EmpleadoRequest empleadoRequest) {
        EmpleadoResponse createdEmpleado = empleadoService.createEmpleado(empleadoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmpleado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLEADO_UPDATE')")
    public ResponseEntity<EmpleadoResponse> updateEmpleado(@PathVariable Long id, @Valid @RequestBody EmpleadoRequest empleadoRequest) {
        EmpleadoResponse updatedEmpleado = empleadoService.updateEmpleado(id, empleadoRequest);
        return ResponseEntity.ok(updatedEmpleado);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('EMPLEADO_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> changeEmpleadoStatus(@PathVariable Long id, @RequestParam Boolean activo) {
        ApiResponse<Void> apiResponse = empleadoService.changeEmpleadoStatus(id, activo);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLEADO_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deactivateEmpleado(@PathVariable Long id) {
        ApiResponse<Void> apiResponse = empleadoService.deactivateEmpleado(id);
        return ResponseEntity.ok(apiResponse);
    }

    // ⭐️ NUEVO: Endpoint para exportar a Excel
    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('EMPLEADO_READ')")
    public ResponseEntity<byte[]> exportEmpleadosToExcel(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws IOException {

        List<EmpleadoExportDTO> empleados = empleadoService.findEmpleadosForExport(filter, sort);

        ByteArrayOutputStream excelStream = exporterService.exportToExcel(empleados, "Reporte deEmpleados");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Empleados.xlsx");
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return ResponseEntity.ok().headers(headers).body(excelStream.toByteArray());
    }

    // ⭐️ NUEVO: Endpoint para exportar a PDF
    @GetMapping("/export/pdf")
    @PreAuthorize("hasAuthority('EMPLEADO_READ')")
    public ResponseEntity<byte[]> exportEmpleadosToPdf(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws DocumentException, IOException {

        List<EmpleadoExportDTO> empleados = empleadoService.findEmpleadosForExport(filter, sort);

        if (empleados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        ByteArrayOutputStream pdfStream = exporterService.exportToPdf(empleados, "Reporte de Empleados");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Empleados.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok().headers(headers).body(pdfStream.toByteArray());
    }
}
