package com.gestionremodelacion.gestion.cliente.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Importar Page
import org.springframework.web.bind.annotation.DeleteMapping; // Importar Pageable
import org.springframework.web.bind.annotation.GetMapping; // Importar PageableDefault
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestionremodelacion.gestion.cliente.dto.request.ClienteRequest;
import com.gestionremodelacion.gestion.cliente.dto.response.ClienteExportDTO;
import com.gestionremodelacion.gestion.cliente.dto.response.ClienteResponse;
import com.gestionremodelacion.gestion.cliente.service.ClienteService;
import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.export.ExporterService;
import com.itextpdf.text.DocumentException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ExporterService exporterService;

    public ClienteController(ClienteService clienteService, ExporterService exporterService) {
        this.clienteService = clienteService;
        this.exporterService = exporterService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENTE_READ')")
    public ResponseEntity<ApiResponse<Page<ClienteResponse>>> getAllClientes(Pageable pageable,
            @RequestParam(name = "filter", required = false) String filter) {
        Page<ClienteResponse> clientesPage = clienteService.getAllClientes(pageable, filter);
        return ResponseEntity.ok(ApiResponse.success(clientesPage));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_READ')")
    public ResponseEntity<ClienteResponse> getClienteById(@PathVariable Long id) {
        ClienteResponse cliente = clienteService.getClienteById(id);
        return ResponseEntity.ok(cliente);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENTE_CREATE')")
    public ResponseEntity<ClienteResponse> createCliente(@Valid @RequestBody ClienteRequest clienteRequest) {
        ClienteResponse createdCliente = clienteService.createCliente(clienteRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCliente);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_UPDATE')")
    public ResponseEntity<ClienteResponse> updateCliente(@PathVariable Long id, @Valid @RequestBody ClienteRequest clienteRequest) {
        ClienteResponse updatedCliente = clienteService.updateCliente(id, clienteRequest);
        return ResponseEntity.ok(updatedCliente);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteCliente(@PathVariable Long id) {
        ApiResponse<Void> apiResponse = clienteService.deleteCliente(id);
        return ResponseEntity.ok(apiResponse);
    }

    // ⭐️ Nuevo endpoint para exportar a Excel
    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('CLIENTE_READ')")
    public ResponseEntity<byte[]> exportClientsToExcel(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws IOException {

        List<ClienteExportDTO> clientes = clienteService.findClientesForExport(filter, sort);
        ByteArrayOutputStream excelStream = exporterService.exportToExcel(clientes, "Reporte de Clientes");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Clientes.xlsx");
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return ResponseEntity.ok().headers(headers).body(excelStream.toByteArray());
    }

    // ⭐️ Nuevo endpoint para exportar a PDF
    @GetMapping("/export/pdf")
    @PreAuthorize("hasAuthority('CLIENTE_READ')")
    public ResponseEntity<byte[]> exportClientsToPdf(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws DocumentException, IOException {

        List<ClienteExportDTO> clientes = clienteService.findClientesForExport(filter, sort);
        if (clientes.isEmpty()) {
            // Devuelve una respuesta HTTP 404 Not Found o 204 No Content
            return ResponseEntity.notFound().build();
        }
        ByteArrayOutputStream pdfStream = exporterService.exportToPdf(clientes, "Reporte de Clientes");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Clientes.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok().headers(headers).body(pdfStream.toByteArray());
    }

}
