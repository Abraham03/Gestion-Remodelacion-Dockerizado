package com.gestionremodelacion.gestion.empresa.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gestionremodelacion.gestion.empresa.model.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    // CORREGIDO: findByNombreEmpresa... en lugar de findByNombre...
    Optional<Empresa> findByNombreEmpresaIgnoreCase(String nombreEmpresa);

    // CORREGIDO: existsByNombreEmpresa... en lugar de existsByNombre...
    boolean existsByNombreEmpresaIgnoreCase(String nombreEmpresa);

    @Query("SELECT e FROM Empresa e WHERE :filter IS NULL OR LOWER(e.nombreEmpresa) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<Empresa> findByFilter(String filter, Pageable pageable);
}
