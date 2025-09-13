// Ubicación: com/gestionremodelacion/gestion/exception/ErrorCatalog.java
package com.gestionremodelacion.gestion.exception;

/**
 * Catálogo centralizado de claves de error para la internacionalización (i18n).
 */
public enum ErrorCatalog {
    // --- Errores Genéricos (Aplicables a cualquier entidad) ---
    RESOURCE_NOT_FOUND("error.resource.notFound"),
    DUPLICATE_RESOURCE("error.duplicate.resource"),
    VALIDATION_ERROR("error.validation"),
    UNEXPECTED_ERROR("error.unexpected"),

    // --- Errores de Clientes ---
    CLIENT_HAS_DEPENDENCIES("error.client.hasDependencies"),
    INVALID_CLIENT_FOR_COMPANY("error.client.invalidForCompany"),

    // --- Errores de Proyectos ---
    PROJECT_HAS_WORK_HOURS("error.project.hasWorkHours"),
    PROJECT_NOT_FOUND("error.project.notFound"),
    INVALID_PROJECT_FOR_COMPANY("error.project.invalidForCompany"),

    // --- Errores de Autenticación y Seguridad ---
    NO_AUTHENTICATED_USER("error.auth.notAuthenticated"),
    AUTHENTICATED_USER_NOT_FOUND("error.auth.userNotFoundInDb"),

    // --- Errores de Usuarios ---
    USER_NOT_FOUND("error.user.notFound"),
    USERNAME_ALREADY_EXISTS("error.user.usernameExists"),
    CANNOT_DELETE_OWN_ACCOUNT("error.user.cannotDeleteSelf"),

    // --- Errores de Roles y Permisos ---
    ROLE_NOT_FOUND("error.role.notFound"),
    ROLE_NAME_ALREADY_EXISTS("error.role.nameExists"),
    PERMISSION_NOT_FOUND("error.permission.notFound"),

    // --- Errores de Empleados ---
    EMPLOYEE_ALREADY_IN_DESIRED_STATE("error.employee.alreadyInDesiredState"),
    INVALID_EMPLOYEE_FOR_COMPANY("error.employee.invalidForCompany"),

    // --- Errores de Horas Trabajadas ---
    WORK_LOG_NOT_FOUND("error.workLog.notFound");

    private final String key;

    ErrorCatalog(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}