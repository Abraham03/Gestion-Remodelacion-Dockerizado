package com.gestionremodelacion.gestion.empleado.controller;

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
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoDropdownResponse;
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoExportDTO;
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoResponse;
import com.gestionremodelacion.gestion.empleado.service.EmpleadoService;
import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.empresa.model.Empresa.PlanSuscripcion;
import com.gestionremodelacion.gestion.export.ExportType;
import com.gestionremodelacion.gestion.export.ExporterService;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.security.annotations.RequiresPlan;
import com.gestionremodelacion.gestion.service.user.UserService;
import com.itextpdf.text.DocumentException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final ExporterService exporterService;
    private final UserService userService;

    public EmpleadoController(EmpleadoService empleadoService, ExporterService exporterService,
            UserService userService) {
        this.empleadoService = empleadoService;
        this.exporterService = exporterService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLEADO_READ')")
    public ResponseEntity<ApiResponse<Page<EmpleadoResponse>>> getAllEmpleados(
            Pageable pageable,
            @RequestParam(name = "filter", required = false) String filter) {
        Page<EmpleadoResponse> page = empleadoService.getAllEmpleados(pageable, filter);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Empleados obtenidos con éxito", page));
    }

    @GetMapping("/dropdown")
    @PreAuthorize("hasAuthority('EMPLEADO_DROPDOWN')")
    public ResponseEntity<ApiResponse<List<EmpleadoDropdownResponse>>> getEmpleadosForDropdown() {
        List<EmpleadoDropdownResponse> empleados = empleadoService.getEmpleadosForDropdown();
        return ResponseEntity
                .ok(new ApiResponse<>(HttpStatus.OK.value(), "Empleados para dropdown obtenidos con éxito", empleados));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLEADO_READ')")
    public ResponseEntity<ApiResponse<EmpleadoResponse>> getEmpleadoById(@PathVariable Long id) {
        EmpleadoResponse empleado = empleadoService.getEmpleadoById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Empleado obtenido con éxito", empleado));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLEADO_CREATE')")
    public ResponseEntity<ApiResponse<EmpleadoResponse>> createEmpleado(
            @Valid @RequestBody EmpleadoRequest empleadoRequest) {
        EmpleadoResponse response = empleadoService.createEmpleado(empleadoRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Empleado creado con éxito", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLEADO_UPDATE')")
    public ResponseEntity<ApiResponse<EmpleadoResponse>> updateEmpleado(@PathVariable Long id,
            @Valid @RequestBody EmpleadoRequest empleadoRequest) {
        EmpleadoResponse response = empleadoService.updateEmpleado(id, empleadoRequest);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Empleado actualizado con éxito", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('EMPLEADO_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> changeEmpleadoStatus(@PathVariable Long id, @RequestParam Boolean activo) {
        empleadoService.changeEmpleadoStatus(id, activo);
        String statusMessage = activo ? "activado" : "desactivado";
        return ResponseEntity
                .ok(new ApiResponse<>(HttpStatus.OK.value(), "Empleado " + statusMessage + " con éxito.", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLEADO_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deactivateEmpleado(@PathVariable Long id) {
        empleadoService.deleteEmpleado(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Empleado desactivado con éxito.", null));
    }

    // Endpoint para exportar a Excel
    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('EXPORT_EXCEL')")
    @RequiresPlan({PlanSuscripcion.NEGOCIOS, PlanSuscripcion.PROFESIONAL})
    public ResponseEntity<byte[]> exportEmpleadosToExcel(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws IOException {

        User currentUser = userService.getCurrentUser();
        Empresa empresa = currentUser.getEmpresa();

        List<EmpleadoExportDTO> empleados = empleadoService.findEmpleadosForExport(filter, sort);

        ByteArrayOutputStream excelStream = exporterService.export(
                ExportType.EXCEL,
                empleados,
                "Reporte deEmpleados",
                empresa
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Empleados.xlsx");
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return ResponseEntity.ok().headers(headers).body(excelStream.toByteArray());
    }

    // Endpoint para exportar a PDF
    @GetMapping("/export/pdf")
    @PreAuthorize("hasAuthority('EXPORT_PDF')")
    @RequiresPlan({PlanSuscripcion.NEGOCIOS, PlanSuscripcion.PROFESIONAL})
    public ResponseEntity<byte[]> exportEmpleadosToPdf(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws DocumentException, IOException {

        User currentUser = userService.getCurrentUser();
        Empresa empresa = currentUser.getEmpresa();

        List<EmpleadoExportDTO> empleados = empleadoService.findEmpleadosForExport(filter, sort);

        if (empleados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        ByteArrayOutputStream pdfStream = exporterService.export(
                ExportType.PDF,
                empleados,
                "Reporte de Empleados",
                empresa
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Empleados.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok().headers(headers).body(pdfStream.toByteArray());
    }
}
