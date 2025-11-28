package com.gestionremodelacion.gestion.invitation.request;

import jakarta.validation.constraints.Email;

public class InvitationRequest {

    @Email(message = "Debe proporcionar un correo electrónico válido.")
    private String email;

    private Long empresaId;

    private String rolAAsignar;

    private Long empleadoId;

    public String getRolAAsignar() {
        return rolAAsignar;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getEmail() {
        return email;
    }

    public Long getEmpleadoId() {
        return empleadoId;
    }

}