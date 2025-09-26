package com.gestionremodelacion.gestion.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}") // Agrega esta URL a tu application.properties
    private String frontendUrl;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvitationEmail(String to, String token, String nombreEmpresa) {
        String registrationUrl = frontendUrl + "/register-invitation?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@gestionremodelacion.com");
        message.setTo(to);
        message.setSubject("Invitación para unirte a " + nombreEmpresa);
        message.setText(
                "Has sido invitado a unirte a la empresa '" + nombreEmpresa + "' en la plataforma de gestión.\n\n" +
                        "Para aceptar la invitación y completar tu registro, haz clic en el siguiente enlace:\n" +
                        registrationUrl + "\n\n" +
                        "Este enlace es válido por 48 horas.\n\n" +
                        "Si no esperabas esta invitación, puedes ignorar este correo.");

        mailSender.send(message);
    }
}