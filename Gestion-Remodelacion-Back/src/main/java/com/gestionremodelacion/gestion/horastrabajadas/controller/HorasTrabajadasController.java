package com.gestionremodelacion.gestion.horastrabajadas.controller;

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
import com.gestionremodelacion.gestion.export.ExporterService;
import com.gestionremodelacion.gestion.horastrabajadas.dto.request.HorasTrabajadasRequest;
import com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasExportDTO;
import com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasResponse;
import com.gestionremodelacion.gestion.horastrabajadas.service.HorasTrabajadasService;
import com.itextpdf.text.DocumentException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/horas-trabajadas")
public class HorasTrabajadasController {

    private final HorasTrabajadasService horasTrabajadasService;
    private final ExporterService exporterService;

    public HorasTrabajadasController(HorasTrabajadasService horasTrabajadasService, ExporterService exportService) {
        this.horasTrabajadasService = horasTrabajadasService;
        this.exporterService = exportService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('HORASTRABAJADAS_READ')")
    public ResponseEntity<ApiResponse<Page<HorasTrabajadasResponse>>> getAllHorasTrabajadas(Pageable pageable,
            @RequestParam(name = "filter", required = false) String filter) {
        Page<HorasTrabajadasResponse> horasTrabajadasPage = horasTrabajadasService.getAllHorasTrabajadas(pageable, filter);
        return ResponseEntity.ok(ApiResponse.success(horasTrabajadasPage));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HORASTRABAJADAS_READ')")
    public ResponseEntity<HorasTrabajadasResponse> getHorasTrabajadasById(@PathVariable Long id) {
        HorasTrabajadasResponse horasTrabajadas = horasTrabajadasService.getHorasTrabajadasById(id);
        return ResponseEntity.ok(horasTrabajadas);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HORASTRABAJADAS_CREATE')")
    public ResponseEntity<HorasTrabajadasResponse> createHorasTrabajadas(@Valid @RequestBody HorasTrabajadasRequest horasTrabajadasRequest) {
        HorasTrabajadasResponse createdHorasTrabajadas = horasTrabajadasService.createHorasTrabajadas(horasTrabajadasRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHorasTrabajadas);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HORASTRABAJADAS_UPDATE')")
    public ResponseEntity<HorasTrabajadasResponse> updateHorasTrabajadas(@PathVariable Long id, @Valid @RequestBody HorasTrabajadasRequest horasTrabajadasRequest) {
        HorasTrabajadasResponse updatedHorasTrabajadas = horasTrabajadasService.updateHorasTrabajadas(id, horasTrabajadasRequest);
        return ResponseEntity.ok(updatedHorasTrabajadas);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HORASTRABAJADAS_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteHorasTrabajadas(@PathVariable Long id) {
        ApiResponse<Void> apiResponse = horasTrabajadasService.deleteHorasTrabajadas(id);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('HORASTRABAJADAS_READ')")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws IOException {

        List<HorasTrabajadasExportDTO> data = horasTrabajadasService.findHorasTrabajadasForExport(filter, sort);
        ByteArrayOutputStream stream = exporterService.exportToExcel(data, "Reporte deHoras Trabajadas");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Horas_Trabajadas.xlsx");
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return ResponseEntity.ok().headers(headers).body(stream.toByteArray());
    }

    /**
     * ✅ NUEVO: Endpoint para exportar a PDF.
     */
    @GetMapping("/export/pdf")
    @PreAuthorize("hasAuthority('HORASTRABAJADAS_READ')")
    public ResponseEntity<byte[]> exportToPdf(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws DocumentException, IOException {

        List<HorasTrabajadasExportDTO> data = horasTrabajadasService.findHorasTrabajadasForExport(filter, sort);
        if (data.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        ByteArrayOutputStream stream = exporterService.exportToPdf(data, "Reporte de Horas Trabajadas");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Horas_Trabajadas.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok().headers(headers).body(stream.toByteArray());
    }

}
