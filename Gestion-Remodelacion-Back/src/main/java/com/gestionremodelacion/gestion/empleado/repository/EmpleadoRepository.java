package com.gestionremodelacion.gestion.empleado.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionremodelacion.gestion.empleado.model.Empleado;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

        /* ======================================================================= */
        /* MÉTODOS PARA EmpleadoService (CRUD, Paginación, etc.) */
        /* ======================================================================= */
        Optional<Empleado> findByIdAndEmpresaId(Long id, Long empresaId);

        Page<Empleado> findAllByEmpresaId(Long empresaId, Pageable pageable);

        List<Empleado> findAllByEmpresaId(Long empresaId, Sort sort);

        boolean existsByIdAndEmpresaId(Long id, Long empresaId);

        List<Empleado> findByEmpresaIdAndActivo(Long empresaId, Boolean activo);

        // Busca empleados de una empresa con paginacion y filtro opcional. el filtro
        // busca en nombreCompleto, rolCargo y telefonoContacto
        @Query("SELECT e FROM Empleado e WHERE e.empresa.id = :empresaId AND " +
                        "(:filter IS NULL OR LOWER(e.nombreCompleto) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
                        "LOWER(e.rolCargo) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
                        "e.telefonoContacto LIKE %:filter%)")
        Page<Empleado> findByEmpresaIdAndFilter(
                        @Param("empresaId") Long empresaId,
                        @Param("filter") String filter,
                        Pageable pageable);

        // Lista para exportar a excel y Pdf, Busca empleados de una empresa sin
        // paginacion y filtro opcional. el filtro busca en nombreCompleto, rolCargo y
        // telefonoContacto
        @Query("SELECT e FROM Empleado e WHERE e.empresa.id = :empresaId AND " +
                        "(:filter IS NULL OR LOWER(e.nombreCompleto) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
                        "LOWER(e.rolCargo) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
                        "e.telefonoContacto LIKE %:filter%)")
        List<Empleado> findByEmpresaIdAndFilter(
                        @Param("empresaId") Long empresaId,
                        @Param("filter") String filter,
                        Sort sort);

        /* ======================================================================= */
        /* MÉTODOS EXCLUSIVOS PARA DashboardService (Agregaciones) */
        /* ======================================================================= */
        @Query("SELECT AVG(e.costoPorHora) FROM Empleado e WHERE e.activo = true")
        Double findAvgCostoPorHora();

        // Query para contar activos por empresaId y activos
        @Query("SELECT COUNT(e) FROM Empleado e WHERE e.empresa.id = :empresaId AND e.activo = true")
        Long countByEmpresaIdAndActivo(@Param("empresaId") Long empresaId);

        // ✅ NUEVO: Métodos para filtrar por año y mes (basado en fecha de contratación)
        @Query("SELECT COUNT(e) FROM Empleado e WHERE YEAR(e.fechaContratacion) = :year")
        Long countByYear(@Param("year") int year);

        @Query("SELECT COUNT(e) FROM Empleado e WHERE YEAR(e.fechaContratacion) = :year AND MONTH(e.fechaContratacion) = :month")
        Long countByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
