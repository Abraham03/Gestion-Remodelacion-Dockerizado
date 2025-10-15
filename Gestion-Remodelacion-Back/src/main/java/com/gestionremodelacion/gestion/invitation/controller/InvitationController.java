package com.gestionremodelacion.gestion.invitation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestionremodelacion.gestion.dto.response.ApiResponse;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.invitation.request.InvitationRequest;
import com.gestionremodelacion.gestion.invitation.response.InvitationDetailsResponse;
import com.gestionremodelacion.gestion.invitation.service.InvitationService;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.service.user.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;
    private final UserService userService;

    public InvitationController(InvitationService invitationService, UserService userService) {
        this.invitationService = invitationService;
        this.userService = userService;
    }

    @PostMapping("/super")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createInvitationBySuperAdmin(
            @Valid @RequestBody InvitationRequest request) {
        invitationService.createAndSendInvitation(request.getEmail(), request.getEmpresaId(),
                request.getRolAAsignar());
        return ResponseEntity.ok(new ApiResponse<>(200, "Invitación enviada por Super Admin.", null));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVITE_USER')")
    public ResponseEntity<ApiResponse<Void>> createInvitationByAdmin(@Valid @RequestBody InvitationRequest request) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getEmpresa() == null) {
            throw new BusinessRuleException("No perteneces a ninguna empresa para poder invitar usuarios.");
        }
        // El rol se fija a "ROLE_USER" por seguridad.
        invitationService.createAndSendInvitation(request.getEmail(), currentUser.getEmpresa().getId(), "ROLE_USER");
        return ResponseEntity.ok(new ApiResponse<>(200, "Invitación enviada.", null));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<InvitationDetailsResponse>> validateInvitationToken(@RequestParam String token) {
        InvitationDetailsResponse details = invitationService.validateToken(token);
        return ResponseEntity.ok(new ApiResponse<>(200, "Token válido", details));
    }

}