package com.gestionremodelacion.gestion.invitation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestionremodelacion.gestion.invitation.model.Invitacion;

@Repository
public interface InvitacionRepository extends JpaRepository<Invitacion, Long> {

    Optional<Invitacion> findByToken(String token);

    boolean existsByEmailAndUtilizadaIsFalse(String email);
}