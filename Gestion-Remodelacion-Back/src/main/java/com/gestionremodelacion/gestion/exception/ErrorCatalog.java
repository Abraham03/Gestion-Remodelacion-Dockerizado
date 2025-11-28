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
    SUPER_ADMIN_ROLE_ASSIGNMENT_NOT_ALLOWED("error.user.uperAdminRoleAssignmentNotAllowed"),
    CANNOT_DELETE_SUPER_ADMIN("error.user.cannotDeleteSuperAdmin"), // error.user.cannotDeleteSuperAdmin
    USER_NOT_LINKED_TO_EMPLOYEE("error.user.notLinkedToEmployee"),
    EMAIL_ALREADY_IN_USE("error.user.emailInUse"),

    // --- Errores de Roles y Permisos ---
    ROLE_NOT_FOUND("error.role.notFound"),
    ROLE_NAME_ALREADY_EXISTS("error.role.nameExists"),
    PERMISSION_NOT_FOUND("error.permission.notFound"),
    PERMISSION_NOT_ALLOWED("error.permission.notAllowed"),
    ROLES_ARE_REQUIRED("error.user.rolesRequired"),

    // --- Errores de Empleados ---
    EMPLOYEE_ALREADY_IN_DESIRED_STATE("error.employee.alreadyInDesiredState"),
    INVALID_EMPLOYEE_FOR_COMPANY("error.employee.invalidForCompany"),
    EMPLOYEE_NOT_FOUND("error.employee.notFound"),
    EMPLOYEE_ALREADY_LINKED_TO_USER("error.employee.alreadyLinkedToUser"),

    // --- Errores de Horas Trabajadas ---
    WORK_LOG_NOT_FOUND("error.workLog.notFound"),
    INVALID_INPUT_UNIT("error.workLog.invalidInputUnit"),

    // --- Errores de Companias ---
    COMPANY_NOT_FOUND("error.company.notFound"),
    COMPANY_HAS_ASSOCIATED_USERS("error.company.hasAssociatedUsers"),
    COMPANY_NAME_ALREADY_EXISTS("error.company.nameExists"),
    COMPANY_ID_REQUIRED("error.company.idRequired"),

    // --- Errores de Subida de Archivos ---
    FILE_UPLOAD_ERROR("error.file.upload"),
    FILE_EMPTY("error.file.empty"),
    FILE_SIZE_EXCEEDED("error.file.sizeExceeded"),
    FILE_INVALID_TYPE("error.file.invalidType"),

    // --- Errores de Invitaciones por email
    INVITATION_EMAIL_ALREADY_EXISTS("error.invitation.emailExists"),
    INVITATION_ALREADY_SENT("error.invitation.alreadySent"),
    VALIDATION_TOKEN_INVALID("error.validation.tokenInvalid"),
    VALIDATION_TOKEN_USED("error.validation.tokenUsed"),
    VALIDATION_TOKEN_EXPIRED("error.validation.tokenExpired");

    private final String key;

    ErrorCatalog(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}