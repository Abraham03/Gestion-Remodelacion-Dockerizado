package com.gestionremodelacion.gestion.empresa.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.gestionremodelacion.gestion.empresa.dto.EmpresaRequest;
import com.gestionremodelacion.gestion.empresa.dto.EmpresaResponse;
import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.empresa.repository.EmpresaRepository;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.exception.DuplicateResourceException;
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.mapper.EmpresaMapper;
import com.gestionremodelacion.gestion.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final EmpresaMapper empresaMapper;

    public EmpresaService(EmpresaRepository empresaRepository, UserRepository userRepository,
            EmpresaMapper empresaMapper) {
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
        this.empresaMapper = empresaMapper;
    }

    @Transactional
    public Page<EmpresaResponse> findAll(Pageable pageable, String filter) {
        String effectiveFilter = (filter != null && !filter.trim().isEmpty()) ? filter.trim() : null;
        return empresaRepository.findByFilter(effectiveFilter, pageable).map(empresaMapper::toDto);
    }

    @Transactional
    public EmpresaResponse findById(Long id) {
        return empresaRepository.findById(id)
                .map(empresaMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.COMPANY_NOT_FOUND.getKey()));
    }

    @Transactional
    public EmpresaResponse create(EmpresaRequest empresaRequest) {
        if (empresaRepository.existsByNombreEmpresaIgnoreCase(empresaRequest.getNombreEmpresa())) {
            throw new DuplicateResourceException(ErrorCatalog.COMPANY_NAME_ALREADY_EXISTS.getKey());
        }
        Empresa nuevaEmpresa = empresaMapper.toEntity(empresaRequest);
        return empresaMapper.toDto(empresaRepository.save(nuevaEmpresa));
    }

    @Transactional
    public EmpresaResponse update(Long id, EmpresaRequest empresaRequest) {
        Empresa empresaExistente = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.COMPANY_NOT_FOUND.getKey()));

        // Valida si el nuevo nombre ya está en uso por OTRA empresa
        empresaRepository.findByNombreEmpresaIgnoreCase(empresaRequest.getNombreEmpresa())
                .ifPresent(empresaConMismoNombre -> {
                    if (!empresaConMismoNombre.getId().equals(id)) {
                        throw new DuplicateResourceException(ErrorCatalog.COMPANY_NAME_ALREADY_EXISTS.getKey());
                    }
                });

        empresaExistente.setNombreEmpresa(empresaRequest.getNombreEmpresa());
        return empresaMapper.toDto(empresaRepository.save(empresaExistente));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCatalog.COMPANY_NOT_FOUND.getKey());
        }
        // Regla de negocio: No permitir borrar una empresa si tiene usuarios asociados
        if (userRepository.existsByEmpresaId(id)) {
            throw new BusinessRuleException(ErrorCatalog.COMPANY_HAS_ASSOCIATED_USERS.getKey());
        }
        empresaRepository.deleteById(id);
    }

    @Transactional
    public void changeStatus(Long id, boolean activo) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.COMPANY_NOT_FOUND.getKey()));
        empresa.setActivo(activo);
        empresaRepository.save(empresa);
    }

    public List<EmpresaResponse> findAllForDropdown() {
        return empresaRepository.findAll().stream()
                .map(empresaMapper::toDto) // Necesitarás crear este DTO y el mapeo
                .collect(Collectors.toList());
    }

}
