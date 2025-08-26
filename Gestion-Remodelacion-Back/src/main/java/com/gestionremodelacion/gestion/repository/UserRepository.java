package com.gestionremodelacion.gestion.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestionremodelacion.gestion.model.User;

/**
 * Repositorio para la entidad User que proporciona operaciones CRUD y consultas
 * personalizadas relacionadas con usuarios.
 */
@Repository

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Page<User> findByUsernameContainingIgnoreCase(String searchTerm, Pageable pageable);
}
