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
import com.gestionremodelacion.gestion.model.User;

/**
 * Repositorio para la entidad User que proporciona operaciones CRUD y consultas
 * personalizadas relacionadas con usuarios.
 */
@Repository

public interface UserRepository extends JpaRepository<User, Long> {
        /**
         * Busca un usuario por su nombre de usuario.
         * Esta es una de las pocas consultas que NO debe filtrar por empresa,
         * ya que los nombres de usuario deben ser únicos en todo el sistema para el
         * login.
         */
        Optional<User> findByUsername(String username);

        /**
         * Verifica si un nombre de usuario ya existe en todo el sistema.
         */
        Boolean existsByUsername(String username);

        Optional<User> findByIdAndEmpresaId(Long id, Long empresaId);

        @Query("SELECT u FROM User u WHERE u.empresa.id = :empresaId AND " +
                        "(:filter IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :filter, '%')))")
        Page<User> findByEmpresaIdAndFilter(
                        @Param("empresaId") Long empresaId,
                        @Param("filter") String filter,
                        Pageable pageable);

        boolean existsByIdAndEmpresaId(Long id, Long empresaId);

        List<User> findByRolesContainingAndEmpresaId(Role role, Long empresaId);

}
