package com.gestionremodelacion.gestion.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestionremodelacion.gestion.model.Role;

/**
 * Repositorio para la entidad Role que proporciona operaciones CRUD y consultas
 * personalizadas relacionadas con roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    Page<Role> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    Boolean existsByName(String name);

}
