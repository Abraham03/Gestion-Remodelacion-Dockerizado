package com.gestionremodelacion.gestion.invitation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.invitation.request.InvitationRequest;
import com.gestionremodelacion.gestion.invitation.response.InvitationDetailsResponse;
import com.gestionremodelacion.gestion.invitation.service.InvitationService;
import com.gestionremodelacion.gestion.service.impl.UserDetailsImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVITE_USER')") // Asegúrate de crear este permiso y asignarlo a los roles de admin
    public ResponseEntity<ApiResponse<Void>> inviteUser(@Valid @RequestBody InvitationRequest invitationRequest) {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        Long empresaId; // <-- Se declara la variable una sola vez aquí.

        boolean isSuperAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            // If the user is a SUPER ADMIN, the 'empresaId' MUST come in the request.
            empresaId = invitationRequest.getEmpresaId();
            if (empresaId == null) {
                throw new BusinessRuleException("Para el SUPER ADMIN, el campo 'empresaId' es obligatorio.");
            }
        } else {
            // If the user is a normal Admin, we take the ID from their own company.
            Empresa empresa = userDetails.getEmpresa();
            if (empresa == null) {
                throw new BusinessRuleException(
                        "El usuario administrador debe pertenecer a una empresa para enviar invitaciones.");
            }
            empresaId = empresa.getId();
        }

        // The redundant block of code that was here has been removed.
        // The logic above already handles all cases correctly.

        invitationService.createAndSendInvitation(invitationRequest.getEmail(), empresaId);

        return ResponseEntity.ok(new ApiResponse<>(200, "Invitación enviada correctamente.", null));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<InvitationDetailsResponse>> validateInvitationToken(@RequestParam String token) {
        InvitationDetailsResponse details = invitationService.validateToken(token);
        return ResponseEntity.ok(new ApiResponse<>(200, "Token válido", details));
    }

}