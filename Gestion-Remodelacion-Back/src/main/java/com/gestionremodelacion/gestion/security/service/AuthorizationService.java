package com.gestionremodelacion.gestion.security.service;

import org.springframework.stereotype.Service;

import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto;

@Service
public class AuthorizationService {

    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * Verifica si el usuario tiene un permiso específico (GENÉRICO). Incluye la
     * lógica de "Super Admin lo puede todo".
     */
    public boolean hasPermission(User user, String permissionName) {
        if (user == null || user.getRoles() == null) {
            return false;
        }

        // 1. Bypass para Super Admin
        if (isSuperAdmin(user)) {
            return true;
        }

        // 2. Verificación estándar de permisos
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    /**
     * Verifica si es Super Admin (Reutilizable).
     */
    public boolean isSuperAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> ROLE_SUPER_ADMIN.equals(role.getName()) || "SUPER_ADMIN".equals(role.getName()));
    }

    // ==========================================
    // VALIDACIONES ESPECÍFICAS (DOMINIO)
    // ==========================================
    /**
     * Valida si un usuario tiene acceso a un PROYECTO específico. Regla: Es
     * Admin O es el Responsable O es parte del Equipo.
     */
    public boolean canAccessProyecto(User user, Proyecto proyecto) {
        if (hasPermission(user, "PROYECTO_READ_ALL") || isSuperAdmin(user)) {
            return true;
        }

        Empleado empleado = user.getEmpleado();
        if (empleado == null) {
            return false;
        }

        boolean esResponsable = proyecto.getEmpleadoResponsable() != null
                && proyecto.getEmpleadoResponsable().getId().equals(empleado.getId());

        boolean esEquipo = proyecto.getEquipoAsignado().stream()
                .anyMatch(e -> e.getId().equals(empleado.getId()));

        return esResponsable || esEquipo;
    }

    /**
     * Valida si un usuario puede ver/editar un EMPLEADO específico. Regla: Es
     * Admin O es el mismo empleado.
     */
    public boolean canAccessEmpleado(User user, Long targetEmpleadoId) {
        if (hasPermission(user, "EMPLEADO_READ_ALL") || isSuperAdmin(user)) {
            return true;
        }
        return user.getEmpleado() != null && user.getEmpleado().getId().equals(targetEmpleadoId);
    }

}
