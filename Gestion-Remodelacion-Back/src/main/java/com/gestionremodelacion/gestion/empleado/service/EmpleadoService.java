package com.gestionremodelacion.gestion.empleado.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.empleado.dto.request.EmpleadoRequest;
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoDropdownResponse;
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoExportDTO;
import com.gestionremodelacion.gestion.empleado.dto.response.EmpleadoResponse;
import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.model.ModeloDePago;
import com.gestionremodelacion.gestion.empleado.repository.EmpleadoRepository;
import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.exception.DuplicateResourceException;
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.mapper.EmpleadoMapper;
import com.gestionremodelacion.gestion.model.Role;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.repository.RoleRepository;
import com.gestionremodelacion.gestion.repository.UserRepository;
import com.gestionremodelacion.gestion.security.service.AuthorizationService;
import com.gestionremodelacion.gestion.service.user.UserService;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final EmpleadoMapper empleadoMapper;
    private final UserService userService;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationService authService;
    private static final String PERMISO_CREATE_ALL = "HORASTRABAJADAS_CREATE_ALL";

    public EmpleadoService(EmpleadoRepository empleadoRepository, EmpleadoMapper empleadoMapper,
            UserService userService, UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, AuthorizationService authService) {
        this.empleadoRepository = empleadoRepository;
        this.empleadoMapper = empleadoMapper;
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<EmpleadoDropdownResponse> getEmpleadosForDropdown() {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        List<Empleado> empleados;

        // 1. Verificamos si tiene permiso de Admin/Manager
        if (authService.hasPermission(currentUser, PERMISO_CREATE_ALL)) {
            // Admin: Ve todos los activos
            empleados = empleadoRepository.findByEmpresaIdAndActivo(empresaId, true);
        } else {
            // Empleado Normal: Solo se ve a sí mismo
            Empleado empleadoVinculado = currentUser.getEmpleado();
            if (empleadoVinculado != null && Boolean.TRUE.equals(empleadoVinculado.getActivo())) {
                empleados = Collections.singletonList(empleadoVinculado);
            } else {
                empleados = Collections.emptyList();
            }
        }

        return empleados.stream()
                .map(emp -> new EmpleadoDropdownResponse(emp.getId(), emp.getNombreCompleto(),
                emp.getModeloDePago().name()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<EmpleadoResponse> getAllEmpleados(Pageable pageable, String filter) {
        User user = userService.getCurrentUser();
        Long empresaId = user.getEmpresa().getId();

        Page<Empleado> empleadosPage = empleadoRepository.findByEmpresaIdAndFilter(empresaId, filter, pageable);

        return empleadosPage.map(empleadoMapper::toEmpleadoResponse);
    }

    @Transactional(readOnly = true)
    public EmpleadoResponse getEmpleadoById(Long id) {
        User user = userService.getCurrentUser();
        Long empresaId = user.getEmpresa().getId();

        return empleadoRepository.findByIdAndEmpresaId(id, empresaId)
                .map(empleadoMapper::toEmpleadoResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.RESOURCE_NOT_FOUND.getKey()));
    }

    @Transactional
    public EmpleadoResponse createEmpleado(EmpleadoRequest empleadoRequest) {
        User currentUser = userService.getCurrentUser();

        Empleado empleado = empleadoMapper.toEmpleado(empleadoRequest);
        empleado.setEmpresa(currentUser.getEmpresa());

        // Obtener el Enum ModeloDePago
        ModeloDePago modeloDePago = ModeloDePago.valueOf(empleadoRequest.getModeloDePago().toUpperCase());
        empleado.setModeloDePago(modeloDePago);

        // Usar el modelo de pago para calcular el costo base (costo/hora)
        BigDecimal costoBasePorHora = modeloDePago.calcularCostoBasePorHora(empleadoRequest.getCostoPorHora());
        empleado.setCostoPorHora(costoBasePorHora);

        if (empleado.getActivo() == null) {
            empleado.setActivo(true);
        }

        // Vinculacion de Usuario nuevo
        // Guarda el empleado Primero para obtener un ID
        Empleado savedEmpleado = empleadoRepository.save(empleado);

        // Si el request incluye un username, crea el usuario asociado
        if (empleadoRequest.getUsername() != null && !empleadoRequest.getUsername().isEmpty()) {
            User newUser = createAndLinkUser(empleadoRequest, savedEmpleado, currentUser.getEmpresa());
            savedEmpleado.setUser(newUser);
        }

        return empleadoMapper.toEmpleadoResponse(savedEmpleado);
    }

    @Transactional
    public EmpleadoResponse updateEmpleado(Long id, EmpleadoRequest empleadoRequest) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Empleado empleado = empleadoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.RESOURCE_NOT_FOUND.getKey()));

        empleadoMapper.updateEmpleadoFromRequest(empleadoRequest, empleado);

        // Obtener el Enum ModeloDePago
        ModeloDePago modeloDePago = ModeloDePago.valueOf(empleadoRequest.getModeloDePago().toUpperCase());
        empleado.setModeloDePago(modeloDePago);

        // Usar el modelo de pago para calcular el costo base (costo/hora)
        BigDecimal costoBasePorHora = modeloDePago.calcularCostoBasePorHora(empleadoRequest.getCostoPorHora());
        empleado.setCostoPorHora(costoBasePorHora);

        Empleado updatedEmpleado = empleadoRepository.save(empleado);
        return empleadoMapper.toEmpleadoResponse(updatedEmpleado);
    }

    @Transactional
    public void changeEmpleadoStatus(Long id, Boolean activo) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Empleado empleado = empleadoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.RESOURCE_NOT_FOUND.getKey()));
        if (empleado.getActivo().equals(activo)) {
            throw new BusinessRuleException(ErrorCatalog.EMPLOYEE_ALREADY_IN_DESIRED_STATE.getKey());
        }
        empleado.setActivo(activo);
        empleadoRepository.save(empleado);
    }

    @Transactional
    public void deleteEmpleado(Long id) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        if (!empleadoRepository.existsByIdAndEmpresaId(id, empresaId)) {
            throw new ResourceNotFoundException(ErrorCatalog.RESOURCE_NOT_FOUND.getKey());
        }
        empleadoRepository.deleteById(id);
    }

    /**
     * Método helper privado para crear y vincular un User al crear un Empleado.
     */
    private User createAndLinkUser(EmpleadoRequest request, Empleado empleado, Empresa empresa) {
        // Validar que el username no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(ErrorCatalog.USERNAME_ALREADY_EXISTS.getKey());
        }
        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(ErrorCatalog.EMAIL_ALREADY_IN_USE.getKey());
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEmpresa(empresa);
        newUser.setEnabled(true);

        // --- EL VÍNCULO CLAVE ---
        newUser.setEmpleado(empleado);

        // Asignar roles
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new BusinessRuleException(ErrorCatalog.ROLES_ARE_REQUIRED.getKey()); // Debes crear este error
        }
        Set<Role> roles = request.getRoles().stream()
                .map(roleId -> roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.ROLE_NOT_FOUND.getKey())))
                .collect(Collectors.toSet());
        newUser.setRoles(roles);

        return userRepository.save(newUser);
    }

    // Método para la exportación
    @Transactional(readOnly = true)
    public List<EmpleadoExportDTO> findEmpleadosForExport(String filter, String sort) {
        User currentUser = userService.getCurrentUser();
        Long empresaId = currentUser.getEmpresa().getId();

        Sort sortObj = Sort.by(Sort.Direction.ASC, "nombreCompleto"); // Orden por defecto
        if (sort != null && !sort.isEmpty()) {
            try {
                String[] sortParts = sort.split(",");
                if (sortParts.length == 2) {
                    String sortProperty = sortParts[0];
                    Sort.Direction sortDirection = "desc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
                    sortObj = Sort.by(sortDirection, sortProperty);
                }
            } catch (Exception e) {
                // Manejar error de parseo, si es necesario, o mantener el sort por defecto
            }
        }

        List<Empleado> empleados = empleadoRepository.findByEmpresaIdAndFilter(empresaId, filter, sortObj);

        return empleados.stream()
                .map(EmpleadoExportDTO::new)
                .collect(Collectors.toList());
    }

}
