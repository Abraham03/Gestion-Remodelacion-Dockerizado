package com.gestionremodelacion.gestion.horastrabajadas.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

        Optional<HorasTrabajadas> findByIdAndEmpresaId(Long id, Long empresaId);

        boolean existsByIdAndEmpresaId(Long id, Long empresaId);

        List<HorasTrabajadas> findByEmpresaId(Long empresaId);

        List<HorasTrabajadas> findByProyectoIdAndEmpresaId(Long proyectoId, Long empresaId);

        // Consulta optimizada que devuelve directamente el DTO.
        @Query("SELECT new com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasResponse("
                        + "h.id, h.empleado.id, h.nombreEmpleado, h.proyecto.id, h.proyecto.nombreProyecto, "
                        + "h.fecha, h.horas, h.costoPorHoraActual, h.actividadRealizada, h.fechaRegistro, "
                        + "h.cantidad, h.unidad) "
                        + "FROM HorasTrabajadas h WHERE h.empresa.id = :empresaId")
        Page<HorasTrabajadasResponse> findAllWithDetails(@Param("empresaId") Long empresaId, Pageable pageable);

        // Consulta optimizada con filtro por nombre de empleado o proyecto.
        @Query("SELECT new com.gestionremodelacion.gestion.horastrabajadas.dto.response.HorasTrabajadasResponse("
                        + "h.id, h.empleado.id, h.nombreEmpleado, h.proyecto.id, h.proyecto.nombreProyecto, "
                        + "h.fecha, h.horas, h.costoPorHoraActual, h.actividadRealizada, h.fechaRegistro,"
                        + "h.cantidad, h.unidad) "
                        + "FROM HorasTrabajadas h WHERE h.empresa.id = :empresaId AND ("
                        + "LOWER(h.nombreEmpleado) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
                        + "LOWER(h.nombreProyecto) LIKE LOWER(CONCAT('%', :filter, '%')))")
        Page<HorasTrabajadasResponse> findByFilterWithDetails(@Param("empresaId") Long empresaId,
                        @Param("filter") String filter, Pageable pageable);

        // Consulta para la exportación que devuelve la entidad completa.
        @Query("SELECT h FROM HorasTrabajadas h JOIN FETCH h.empleado e JOIN FETCH h.proyecto p "
                        + "WHERE h.empresa.id = :empresaId AND (:filter IS NULL OR "
                        + "LOWER(h.nombreEmpleado) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
                        + "LOWER(h.nombreProyecto) LIKE LOWER(CONCAT('%', :filter, '%')))")
        List<HorasTrabajadas> findByFilterForExport(@Param("empresaId") Long empresaId, @Param("filter") String filter,
                        Sort sort);

        /**
         * Calcula la suma total de mano de obra para un proyecto directamente en la BD.
         */
        @Query("SELECT SUM(h.horas * h.costoPorHoraActual) FROM HorasTrabajadas h " +
                        "WHERE h.proyecto.id = :proyectoId AND h.empresa.id = :empresaId")
        BigDecimal sumCostoManoDeObraByProyectoId(@Param("proyectoId") Long proyectoId,
                        @Param("empresaId") Long empresaId);

        /* ======================================================================= */
        /* MÉTODOS EXCLUSIVOS PARA DashboardService (Agregaciones) */
        /* ======================================================================= */
        // -- Consultas filtradas por AÑO --
        @Query("SELECT e.rolCargo, COUNT(DISTINCT e.id) FROM HorasTrabajadas h JOIN h.empleado e " +
                        "WHERE h.empresa.id = :empresaId AND YEAR(h.fecha) = :year GROUP BY e.rolCargo")
        List<Object[]> countEmpleadosByRolByYear(@Param("empresaId") Long empresaId, @Param("year") int year);

        @Query("SELECT h.proyecto.nombreProyecto, SUM(h.horas) FROM HorasTrabajadas h " +
                        "WHERE h.empresa.id = :empresaId AND YEAR(h.proyecto.fechaInicio) = :year GROUP BY h.proyecto.nombreProyecto")
        List<Object[]> sumHorasByProyectoByYear(@Param("empresaId") Long empresaId, @Param("year") int year);

        @Query("SELECT h.empleado.id, h.empleado.nombreCompleto, h.proyecto.id, h.proyecto.nombreProyecto, SUM(h.horas), SUM(h.horas * h.costoPorHoraActual) "
                        +
                        "FROM HorasTrabajadas h WHERE h.empresa.id = :empresaId AND YEAR(h.proyecto.fechaInicio) = :year "
                        +
                        "GROUP BY h.empleado.id, h.empleado.nombreCompleto, h.proyecto.id, h.proyecto.nombreProyecto")
        List<Object[]> sumHorasByEmpleadoAndProyectoByYear(@Param("empresaId") Long empresaId, @Param("year") int year);

        // --- Consultas filtradas por AÑO Y MES ---

        @Query("SELECT e.rolCargo, COUNT(DISTINCT e.id) FROM HorasTrabajadas h JOIN h.empleado e " +
                        "WHERE h.empresa.id = :empresaId AND YEAR(h.fecha) = :year AND MONTH(h.fecha) = :month GROUP BY e.rolCargo")
        List<Object[]> countEmpleadosByRolByYearAndMonth(@Param("empresaId") Long empresaId, @Param("year") int year,
                        @Param("month") int month);

        @Query("SELECT h.proyecto.nombreProyecto, SUM(h.horas) FROM HorasTrabajadas h " +
                        "WHERE h.empresa.id = :empresaId AND YEAR(h.proyecto.fechaInicio) = :year AND MONTH(h.proyecto.fechaInicio) = :month "
                        +
                        "GROUP BY h.proyecto.nombreProyecto")
        List<Object[]> sumHorasByProyectoByYearAndMonth(@Param("empresaId") Long empresaId, @Param("year") int year,
                        @Param("month") int month);

        @Query("SELECT h.empleado.id, h.empleado.nombreCompleto, h.proyecto.id, h.proyecto.nombreProyecto, SUM(h.horas), SUM(h.horas * h.costoPorHoraActual) "
                        +
                        "FROM HorasTrabajadas h WHERE h.empresa.id = :empresaId AND YEAR(h.proyecto.fechaInicio) = :year AND MONTH(h.proyecto.fechaInicio) = :month "
                        +
                        "GROUP BY h.empleado.id, h.empleado.nombreCompleto, h.proyecto.id, h.proyecto.nombreProyecto")
        List<Object[]> sumHorasByEmpleadoAndProyectoByYearAndMonth(@Param("empresaId") Long empresaId,
                        @Param("year") int year, @Param("month") int month);

        // --- Consultas filtradas por ID de Proyecto ---

        @Query("SELECT e.rolCargo, COUNT(DISTINCT e.id) FROM HorasTrabajadas h JOIN h.empleado e " +
                        "WHERE h.empresa.id = :empresaId AND h.proyecto.id = :projectId GROUP BY e.rolCargo")
        List<Object[]> countEmpleadosByRolByProjectId(@Param("empresaId") Long empresaId,
                        @Param("projectId") Long projectId);

        @Query("SELECT h.proyecto.nombreProyecto, SUM(h.horas) FROM HorasTrabajadas h " +
                        "WHERE h.empresa.id = :empresaId AND h.proyecto.id = :projectId GROUP BY h.proyecto.nombreProyecto")
        List<Object[]> sumHorasByProyectoByProjectId(@Param("empresaId") Long empresaId,
                        @Param("projectId") Long projectId);

        @Query("SELECT h.empleado.id, h.empleado.nombreCompleto, h.proyecto.id, h.proyecto.nombreProyecto, SUM(h.horas), SUM(h.horas * h.costoPorHoraActual) "
                        +
                        "FROM HorasTrabajadas h WHERE h.empresa.id = :empresaId AND h.proyecto.id = :projectId " +
                        "GROUP BY h.empleado.id, h.empleado.nombreCompleto, h.proyecto.id, h.proyecto.nombreProyecto")
        List<Object[]> sumHorasByEmpleadoAndProyectoByProjectId(@Param("empresaId") Long empresaId,
                        @Param("projectId") Long projectId);
}
