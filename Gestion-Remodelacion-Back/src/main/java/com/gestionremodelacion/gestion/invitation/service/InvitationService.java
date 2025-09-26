package com.gestionremodelacion.gestion.invitation.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.empresa.repository.EmpresaRepository;
import com.gestionremodelacion.gestion.exception.BusinessRuleException;
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.ResourceNotFoundException;
import com.gestionremodelacion.gestion.invitation.model.Invitacion;
import com.gestionremodelacion.gestion.invitation.repository.InvitacionRepository;
import com.gestionremodelacion.gestion.invitation.response.InvitationDetailsResponse;
import com.gestionremodelacion.gestion.notification.NotificationService;
import com.gestionremodelacion.gestion.repository.UserRepository;

@Service
public class InvitationService {

    private final InvitacionRepository invitacionRepository;
    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public InvitationService(InvitacionRepository invitacionRepository, EmpresaRepository empresaRepository,
            UserRepository userRepository, NotificationService notificationService) {
        this.invitacionRepository = invitacionRepository;
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void createAndSendInvitation(String email, Long empresaId) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException(ErrorCatalog.INVITATION_EMAIL_ALREADY_EXISTS.getKey());
        }

        if (invitacionRepository.existsByEmailAndUtilizadaIsFalse(email)) {
            throw new BusinessRuleException(ErrorCatalog.INVITATION_ALREADY_SENT.getKey());
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.COMPANY_NOT_FOUND.getKey()));

        Invitacion invitacion = new Invitacion();
        invitacion.setEmail(email);
        invitacion.setToken(UUID.randomUUID().toString());
        invitacion.setEmpresa(empresa);
        invitacion.setFechaExpiracion(LocalDateTime.now().plusHours(48));
        invitacionRepository.save(invitacion);

        notificationService.sendInvitationEmail(
                invitacion.getEmail(),
                invitacion.getToken(),
                empresa.getNombreEmpresa());
    }

    public InvitationDetailsResponse validateToken(String token) {
        Invitacion invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(() -> new BusinessRuleException(ErrorCatalog.VALIDATION_TOKEN_INVALID.getKey()));

        if (invitacion.isUtilizada()) {
            throw new BusinessRuleException(ErrorCatalog.VALIDATION_TOKEN_USED.getKey());
        }
        if (invitacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException(ErrorCatalog.VALIDATION_TOKEN_EXPIRED.getKey());
        }

        return new InvitationDetailsResponse(invitacion.getEmail());
    }

}