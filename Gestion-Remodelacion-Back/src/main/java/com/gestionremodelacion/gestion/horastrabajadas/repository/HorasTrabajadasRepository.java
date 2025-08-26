package com.gestionremodelacion.gestion.horastrabajadas.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasResponse;
import com.gestionremodelacion.gestion.horastrabajadas.model.HorasTrabajadas;

@Repository
public interface HorasTrabajadasRepository extends JpaRepository<HorasTrabajadas, Long> {

    List<HorasTrabajadas> findByEmpleadoId(Long empleadoId);

    List<HorasTrabajadas> findByProyectoId(Long proyectoId);

    List<HorasTrabajadas> findByFechaBetween(Date startDate, Date endDate);

    List<HorasTrabajadas> findByEmpleadoIdAndFechaBetween(Long empleadoId, Date startDate, Date endDate);

    List<HorasTrabajadas> findByProyectoIdAndFechaBetween(Long proyectoId, Date startDate, Date endDate);

    // ✅ NUEVO: Consulta optimizada que devuelve directamente el DTO.
    @Query("SELECT new com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasResponse("
            + "h.id, e.id, e.nombreCompleto, p.id, p.nombreProyecto, "
            + "h.fecha, h.horas, h.actividadRealizada, h.fechaRegistro) "
            + "FROM HorasTrabajadas h JOIN h.empleado e JOIN h.proyecto p")
    Page<HorasTrabajadasResponse> findAllWithDetails(Pageable pageable);

    // ✅ NUEVO: Consulta optimizada con filtro por nombre de empleado o proyecto.
    @Query("SELECT new com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasResponse("
            + "h.id, e.id, e.nombreCompleto, p.id, p.nombreProyecto, "
            + "h.fecha, h.horas, h.actividadRealizada, h.fechaRegistro) "
            + "FROM HorasTrabajadas h JOIN h.empleado e JOIN h.proyecto p WHERE "
            + "LOWER(e.nombreCompleto) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
            + "LOWER(p.nombreProyecto) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<HorasTrabajadasResponse> findByFilterWithDetails(@Param("filter") String filter, Pageable pageable);

    // ✅ NUEVO: Consulta para la exportación que devuelve la entidad completa.
    @Query("SELECT h FROM HorasTrabajadas h JOIN FETCH h.empleado e JOIN FETCH h.proyecto p WHERE "
            + "LOWER(e.nombreCompleto) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
            + "LOWER(p.nombreProyecto) LIKE LOWER(CONCAT('%', :filter, '%'))")
    List<HorasTrabajadas> findByFilterForExport(@Param("filter") String filter, Sort sort);

    /* ======================================================================= */
 /* MÉTODOS EXCLUSIVOS PARA DashboardService (Agregaciones)                 */
 /* ======================================================================= */
    // -- Consultas filtradas por AÑO --
    @Query("SELECT h.proyecto.nombreProyecto, SUM(h.horas) FROM HorasTrabajadas h WHERE YEAR(h.proyecto.fechaInicio) = :year GROUP BY h.proyecto.nombreProyecto")
    List<Object[]> sumHorasByProyectoByYear(@Param("year") int year);

    @Query("SELECT h.empleado.nombreCompleto, SUM(h.horas) FROM HorasTrabajadas h WHERE YEAR(h.proyecto.fechaInicio) = :year GROUP BY h.empleado.nombreCompleto")
    List<Object[]> sumHorasByEmpleadoByYear(@Param("year") int year);

    @Query("SELECT h.empleado.id, h.empleado.nombreCompleto, h.proyecto.id, h.proyecto.nombreProyecto, SUM(h.horas) FROM HorasTrabajadas h WHERE YEAR(h.proyecto.fechaInicio) = :year GROUP BY h.empleado.id, h.empleado.nombreCompleto, h.proyecto.id, h.proyecto.nombreProyecto")
    List<Object[]> sumHorasByEmpleadoAndProyectoByYear(@Param("year") int year);

    // -- ✅ NUEVO: Consultas filtradas por AÑO Y MES --
    @Query("SELECT h.proyecto.nombreProyecto, SUM(h.horas) FROM HorasTrabajadas h WHERE YEAR(h.proyecto.fechaInicio) = :year AND MONTH(h.proyecto.fechaInicio) = :month GROUP BY h.proyecto.nombreProyecto")
    List<Object[]> sumHorasByProyectoByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT h.empleado.nombreCompleto, SUM(h.horas) FROM HorasTrabajadas h WHERE YEAR(h.proyecto.fechaInicio) = :year AND MONTH(h.proyecto.fechaInicio) = :month GROUP BY h.empleado.nombreCompleto")
    List<Object[]> sumHorasByEmpleadoByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT h.empleado.id, h.empleado.nombreCompleto, h.proyecto.id, h.proyecto.nombreProyecto, SUM(h.horas) FROM HorasTrabajadas h WHERE YEAR(h.proyecto.fechaInicio) = :year AND MONTH(h.proyecto.fechaInicio) = :month GROUP BY h.empleado.id, h.empleado.nombreCompleto, h.proyecto.id, h.proyecto.nombreProyecto")
    List<Object[]> sumHorasByEmpleadoAndProyectoByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
