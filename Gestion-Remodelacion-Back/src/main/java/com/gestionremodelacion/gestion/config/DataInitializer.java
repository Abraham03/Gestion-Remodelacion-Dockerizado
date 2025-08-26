package com.gestionremodelacion.gestion.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.model.Permission;
import com.gestionremodelacion.gestion.model.Role;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.repository.PermissionRepository;
import com.gestionremodelacion.gestion.repository.RoleRepository;
import com.gestionremodelacion.gestion.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void init() {
        // 1. Definir y guardar permisos
        Set<String> allPermissionsNames = new HashSet<>(Arrays.asList(
                "USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DELETE",
                "ROLE_READ", "ROLE_CREATE", "ROLE_UPDATE", "ROLE_DELETE",
                "PERMISSION_READ", "PERMISSION_CREATE", "PERMISSION_UPDATE", "PERMISSION_DELETE",
                "PROYECTO_READ", "PROYECTO_CREATE", "PROYECTO_UPDATE", "PROYECTO_DELETE",
                "CLIENTE_READ", "CLIENTE_CREATE", "CLIENTE_UPDATE", "CLIENTE_DELETE",
                "EMPLEADO_READ", "EMPLEADO_CREATE", "EMPLEADO_UPDATE", "EMPLEADO_DELETE",
                "DASHBOARD_VIEW", "HORASTRABAJADAS_READ", "HORASTRABAJADAS_CREATE", "HORASTRABAJADAS_UPDATE", "HORASTRABAJADAS_DELETE",
                "USER_UPDATE_ROLES" // Añadir si decides usarlo para granularidad
        ));

        // Obtener o crear todos los permisos y asegurarse de que estén gestionados por JPA
        Set<Permission> allPermissions = allPermissionsNames.stream()
                .map(name -> permissionRepository.findByName(name) // Esto retorna Optional<Permission>
                .orElseGet(() -> { // Si el Optional está vacío, se ejecuta este Supplier
                    Permission newPermission = new Permission();
                    newPermission.setName(name);
                    newPermission.setDescription("Permiso para " + name.replace("_", " ").toLowerCase());
                    return permissionRepository.save(newPermission); // Guarda y retorna el nuevo permiso
                }))
                .collect(Collectors.toSet());

        // 2. Definir y guardar el rol ADMIN y asignar todos los permisos
        // Usar orElse(null) y luego verificar si es null es una opción, pero orElseGet es más idiomático.
        // Aquí lo mantenemos como lo tenías, pero `Optional.ofNullable(roleRepository.findByName("ADMIN").orElse(null))`
        // sería la forma correcta si `findByName` en `RoleRepository` retorna directamente `Role` y no `Optional<Role>`.
        // Asumiendo que `roleRepository.findByName("ADMIN")` retorna `Optional<Role>`:
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newAdminRole = new Role();
                    newAdminRole.setName("ADMIN");
                    newAdminRole.setDescription("Administrador del sistema con acceso completo.");
                    newAdminRole.setPermissions(allPermissions); // Asigna todos los permisos al rol nuevo
                    return roleRepository.save(newAdminRole);
                });

        // Si el rol ADMIN ya existía, asegúrate de que tenga todos los permisos
        // Esto es importante si añades nuevos permisos en futuras versiones y quieres que ADMIN los tenga automáticamente.
        // Solo actualiza si el conjunto de permisos difiere para evitar writes innecesarios.
        if (!adminRole.getPermissions().equals(allPermissions)) {
            adminRole.setPermissions(allPermissions);
            roleRepository.save(adminRole);
        }

        // 3. Crear el usuario ADMIN si no existe y asignarle el rol ADMIN
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            // ¡IMPORTANTE: Cambia esta contraseña en producción!
            adminUser.setPassword(passwordEncoder.encode("admin123"));

            // Aquí usamos la misma instancia 'adminRole' que ya fue obtenida/creada
            // y está gestionada por la transacción.
            adminUser.setRoles(Set.of(adminRole)); // Usar directamente 'adminRole'
            userRepository.save(adminUser);
        }
    }
}
