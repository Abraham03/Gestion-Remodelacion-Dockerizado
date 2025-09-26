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

        /**
         * Métodos para buscar roles DENTRO de una empresa específica.
         * Esto es crucial para que un ADMIN no pueda ver o crear roles con nombres
         * duplicados que pertenecen a otra empresa.
         */
        Optional<Role> findByNameAndEmpresaId(String name, Long empresaId);

        boolean existsByNameAndEmpresaId(String name, Long empresaId);

        /**
         * Busca roles que pertenezcan a una empresa
         * específica Y que no sean el rol SUPER_ADMIN.
         */

        Optional<Role> findByName(String name);

        @Query("SELECT r FROM Role r WHERE r.empresa.id = :empresaId AND r.name <> 'ROLE_SUPER_ADMIN' AND " +
                        "(:searchTerm IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
        Page<Role> findByEmpresaIdAndFilter(@Param("empresaId") Long empresaId, @Param("searchTerm") String searchTerm,
                        Pageable pageable);

        /**
         * Consulta para poblar los dropdowns en el formulario de usuario,
         * asegurando que un ADMIN solo pueda asignar roles de su propia empresa.
         */
        @Query("SELECT r FROM Role r WHERE r.empresa.id = :empresaId AND r.name <> 'ROLE_SUPER_ADMIN'")
        List<Role> findAllByEmpresaId(@Param("empresaId") Long empresaId);

        // Se necesita un método para buscar un rol global por nombre (como
        // ROLE_SUPER_ADMIN)
        Optional<Role> findByNameAndEmpresaIsNull(String name);

        // Consulta para SUPER_ADMIN busca en todos los roles
        Page<Role> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description,
                        Pageable pageable);

}
