package com.gestionremodelacion.gestion.invitation.request;

import jakarta.validation.constraints.Email;

public class InvitationRequest {

    @Email(message = "Debe proporcionar un correo electrónico válido.")
    private String email;

    private Long empresaId;

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getEmail() {
        return email;
    }
}