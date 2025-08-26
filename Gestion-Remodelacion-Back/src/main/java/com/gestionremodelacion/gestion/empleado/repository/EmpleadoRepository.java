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
 /* MÉTODOS PARA EmpleadoService (CRUD, Paginación, etc.)                   */
 /* ======================================================================= */
    Optional<Empleado> findByNombreCompleto(String nombre);

    List<Empleado> findByActivo(Boolean activo);

    long countByActivo(boolean activo);

    Page<Empleado> findByNombreCompletoContainingIgnoreCaseOrRolCargoContainingIgnoreCaseOrTelefonoContactoContainingIgnoreCase(
            String nombre, String rol, String telefono, Pageable pageable);

    List<Empleado> findByNombreCompletoContainingIgnoreCaseOrRolCargoContainingIgnoreCaseOrTelefonoContactoContainingIgnoreCase(
            String nombre, String rol, String telefono, Sort sort);

    /* ======================================================================= */
 /* MÉTODOS EXCLUSIVOS PARA DashboardService (Agregaciones)                 */
 /* ======================================================================= */
    @Query("SELECT AVG(e.costoPorHora) FROM Empleado e WHERE e.activo = true")
    Double findAvgCostoPorHora();

    @Query("SELECT e.rolCargo, COUNT(e) FROM Empleado e GROUP BY e.rolCargo")
    List<Object[]> countEmpleadosByRol();

    // ✅ NUEVO: Métodos para filtrar por año y mes (basado en fecha de contratación)
    @Query("SELECT COUNT(e) FROM Empleado e WHERE YEAR(e.fechaContratacion) = :year")
    Long countByYear(@Param("year") int year);

    @Query("SELECT COUNT(e) FROM Empleado e WHERE YEAR(e.fechaContratacion) = :year AND MONTH(e.fechaContratacion) = :month")
    Long countByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
