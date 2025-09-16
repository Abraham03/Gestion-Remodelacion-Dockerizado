package com.gestionremodelacion.gestion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionremodelacion.gestion.model.Role;

/**
 * Repositorio para la entidad Role que proporciona operaciones CRUD y consultas
 * personalizadas relacionadas con roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    Page<Role> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description,
            Pageable pageable);

    Boolean existsByName(String name);

    // ✅ NUEVO MÉTODO CON JPQL PARA BUSCAR ROLES EXCLUYENDO AL SUPER ADMIN
    @Query("SELECT r FROM Role r WHERE r.name <> 'ROLE_SUPER_ADMIN' AND " +
            "(:searchTerm IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Role> findTenantRoles(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ✅ NUEVO MÉTODO PARA EL FORMULARIO DE USUARIOS (SIN PAGINACIÓN)
    @Query("SELECT r FROM Role r WHERE r.name <> 'ROLE_SUPER_ADMIN'")
    List<Role> findAllTenantRolesForForm();

}
