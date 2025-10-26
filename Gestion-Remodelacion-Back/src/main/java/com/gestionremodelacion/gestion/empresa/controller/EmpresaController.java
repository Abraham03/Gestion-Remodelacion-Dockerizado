package com.gestionremodelacion.gestion.empresa.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.multipart.MultipartFile;

import com.gestionremodelacion.gestion.empresa.dto.EmpresaRequest;
import com.gestionremodelacion.gestion.empresa.dto.EmpresaResponse;
import com.gestionremodelacion.gestion.empresa.dto.EmpresaSimpleResponse;
import com.gestionremodelacion.gestion.empresa.service.EmpresaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {
    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<Page<EmpresaResponse>> getAllEmpresas(
            @PageableDefault(page = 0, size = 10, sort = "nombreEmpresa") Pageable pageable,
            @RequestParam(required = false) String filter) {
        Page<EmpresaResponse> empresas = empresaService.findAll(pageable, filter);
        return ResponseEntity.ok(empresas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPRESA_READ')")
    public ResponseEntity<EmpresaResponse> getEmpresaById(@PathVariable Long id) {
        EmpresaResponse empresa = empresaService.findById(id);
        return ResponseEntity.ok(empresa);
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPRESA_CREATE')")
    public ResponseEntity<EmpresaResponse> createEmpresa(@Valid @RequestBody EmpresaRequest empresaRequest) {
        EmpresaResponse nuevaEmpresa = empresaService.create(empresaRequest);
        return new ResponseEntity<>(nuevaEmpresa, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPRESA_UPDATE')")
    public ResponseEntity<EmpresaResponse> updateEmpresa(@PathVariable Long id,
            @Valid @RequestBody EmpresaRequest empresaRequest) {
        EmpresaResponse empresaActualizada = empresaService.update(id, empresaRequest);
        return ResponseEntity.ok(empresaActualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPRESA_DELETE')")
    public ResponseEntity<Void> deleteEmpresa(@PathVariable Long id) {
        empresaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('EMPRESA_UPDATE')")
    public ResponseEntity<Void> changeEmpresaStatus(@PathVariable Long id, @RequestParam boolean activo) {
        empresaService.changeStatus(id, activo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dropdown")
    @PreAuthorize("hasAuthority('EMPRESA_DROPDOWN')")
    public ResponseEntity<List<EmpresaSimpleResponse>> getAllEmpresasForDropdown() {
        List<EmpresaSimpleResponse> empresas = empresaService.findAllForDropdown();
        return ResponseEntity.ok(empresas);
    }

    @PostMapping("/{id}/logo")
    @PreAuthorize("hasAuthority('EMPRESA_UPDATE_LOGO')")
    public ResponseEntity<String> uploadLogo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        String logoUrl = empresaService.uploadAndSetLogo(id, file);
        return ResponseEntity.ok(logoUrl);
    }
}
