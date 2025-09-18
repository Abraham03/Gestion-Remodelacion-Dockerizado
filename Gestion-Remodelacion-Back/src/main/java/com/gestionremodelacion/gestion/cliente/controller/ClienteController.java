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

import com.gestionremodelacion.gestion.cliente.dto.request.ClienteRequest;
import com.gestionremodelacion.gestion.cliente.dto.response.ClienteExportDTO;
import com.gestionremodelacion.gestion.cliente.dto.response.ClienteResponse;
import com.gestionremodelacion.gestion.cliente.service.ClienteService;
import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.empresa.model.Empresa.PlanSuscripcion;
import com.gestionremodelacion.gestion.export.ExporterService;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.security.annotations.RequiresPlan;
import com.gestionremodelacion.gestion.service.user.UserService;
import com.itextpdf.text.DocumentException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ExporterService exporterService;
    private final UserService userService;

    public ClienteController(ClienteService clienteService, ExporterService exporterService, UserService userService) {
        this.clienteService = clienteService;
        this.exporterService = exporterService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENTE_READ')")
    public ResponseEntity<ApiResponse<Page<ClienteResponse>>> getAllClientes(Pageable pageable,
            @RequestParam(name = "filter", required = false) String filter) {
        Page<ClienteResponse> page = clienteService.getAllClientes(pageable, filter);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Clientes obtenidos con éxito", page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_READ')")
    public ResponseEntity<ApiResponse<ClienteResponse>> getClienteById(@PathVariable Long id) {
        ClienteResponse cliente = clienteService.getClienteById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cliente obtenido con éxito", cliente));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENTE_CREATE')")
    public ResponseEntity<ApiResponse<ClienteResponse>> createCliente(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.createCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Cliente creado con éxito", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_UPDATE')")
    public ResponseEntity<ApiResponse<ClienteResponse>> updateCliente(@PathVariable Long id,
            @Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.updateCliente(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cliente actualizado con éxito", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteCliente(@PathVariable Long id) {
        clienteService.deleteCliente(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cliente eliminado con éxito", null));
    }

    // Nuevo endpoint para exportar a Excel
    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('CLIENTE_READ')")
    @RequiresPlan({ PlanSuscripcion.NEGOCIOS, PlanSuscripcion.PROFESIONAL })
    public ResponseEntity<byte[]> exportClientsToExcel(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws IOException {

        User currentUser = userService.getCurrentUser();
        Empresa empresa = currentUser.getEmpresa();

        List<ClienteExportDTO> clientes = clienteService.findClientesForExport(filter, sort);
        ByteArrayOutputStream excelStream = exporterService.exportToExcel(clientes, "Reporte de Clientes", empresa);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Clientes.xlsx");
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return ResponseEntity.ok().headers(headers).body(excelStream.toByteArray());
    }

    // Nuevo endpoint para exportar a PDF
    @GetMapping("/export/pdf")
    @PreAuthorize("hasAuthority('CLIENTE_READ')")
    @RequiresPlan({ PlanSuscripcion.NEGOCIOS, PlanSuscripcion.PROFESIONAL })
    public ResponseEntity<byte[]> exportClientsToPdf(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "sort", required = false) String sort) throws DocumentException, IOException {

        User currentUser = userService.getCurrentUser();
        Empresa empresa = currentUser.getEmpresa();

        List<ClienteExportDTO> clientes = clienteService.findClientesForExport(filter, sort);
        if (clientes.isEmpty()) {
            // Devuelve una respuesta HTTP 404 Not Found o 204 No Content
            return ResponseEntity.notFound().build();
        }
        ByteArrayOutputStream pdfStream = exporterService.exportToPdf(clientes, "Reporte de Clientes", empresa);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Clientes.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok().headers(headers).body(pdfStream.toByteArray());
    }

}
