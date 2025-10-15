package com.gestionremodelacion.gestion.empresa.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gestionremodelacion.gestion.empresa.dto.EmpresaRequest;
import com.gestionremodelacion.gestion.empresa.dto.EmpresaResponse;
import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.empresa.repository.EmpresaRepository;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.exception.DuplicateResourceException;
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.mapper.EmpresaMapper;
import com.gestionremodelacion.gestion.model.Permission;
import com.gestionremodelacion.gestion.model.Permission.PermissionScope;
import com.gestionremodelacion.gestion.model.Role;
import com.gestionremodelacion.gestion.repository.PermissionRepository;
import com.gestionremodelacion.gestion.repository.RoleRepository;
import com.gestionremodelacion.gestion.repository.UserRepository;
import com.gestionremodelacion.gestion.service.FileUploadService;

import jakarta.transaction.Transactional;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final EmpresaMapper empresaMapper;
    private final FileUploadService fileUploadService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public EmpresaService(EmpresaRepository empresaRepository, UserRepository userRepository,
            EmpresaMapper empresaMapper, @Autowired(required = false) @Nullable FileUploadService fileUploadService,
            RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
        this.empresaMapper = empresaMapper;
        this.fileUploadService = fileUploadService;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
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
        nuevaEmpresa.setLogoUrl(empresaRequest.getLogoUrl());
        Empresa savedEmpresa = empresaRepository.save(nuevaEmpresa);

        // --- Creacion y Asignacion del Rol ADMIN con los permisos TENANT ---
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrador de la empresa" + savedEmpresa.getNombreEmpresa());
        adminRole.setEmpresa(savedEmpresa);

        List<Permission> tenantPermissions = permissionRepository.findByScope(PermissionScope.TENANT, Sort.by("name"));
        adminRole.setPermissions(new HashSet<>(tenantPermissions));

        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        userRole.setDescription("Usuario estándar de la empresa " + savedEmpresa.getNombreEmpresa());
        userRole.setEmpresa(savedEmpresa);

        // Asignarle un conjunto MÍNIMO de permisos. ¡Tú decides cuáles!
        // Por ejemplo, solo leer proyectos y crear horas.
        List<Permission> basicPermissions = permissionRepository
                .findAllByNameIn(List.of("HORASTRABAJADAS_READ", "HORASTRABAJADAS_CREATE", "HORASTRABAJADAS_UPDATE"));
        userRole.setPermissions(new HashSet<>(basicPermissions));
        roleRepository.save(userRole);

        return empresaMapper.toDto(savedEmpresa);
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

        empresaMapper.updateEntityFromDto(empresaRequest, empresaExistente);
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

    /**
     * MÉTODO PARA EL SUPER ADMIN
     * Sube un archivo de logo para una empresa específica y guarda la URL.
     *
     * @param empresaId El ID de la empresa a la que pertenece el logo.
     * @param file      El archivo del logo enviado.
     * @return La URL pública del logo subido.
     * @throws IOException Si ocurre un error al subir el archivo.
     */
    @Transactional
    public String uploadAndSetLogo(Long empresaId, MultipartFile file) throws IOException {
        if (fileUploadService == null) {
            throw new IllegalStateException(
                    "La funcionalidad de subida de archivos no está habilitada en este entorno.");
        }
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.COMPANY_NOT_FOUND.getKey()));

        // La lógica de subida de archivo ahora está contenida y maneja sus propios
        // errores.
        String logoUrl = fileUploadService.uploadFile(file);

        empresa.setLogoUrl(logoUrl);
        empresaRepository.save(empresa);

        return logoUrl;
    }

}
