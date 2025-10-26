package com.gestionremodelacion.gestion.proyecto.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page; // Importar Page
import org.springframework.data.domain.Pageable; // Importar Pageable
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoResponse;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

        Optional<Proyecto> findByIdAndEmpresaId(Long proyectoId, Long empresaId);

        List<Proyecto> findByEmpresaId(Long empresaId);

        boolean existsByIdAndEmpresaId(Long id, Long empresaId);

        @Query("SELECT new com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoResponse("
                        + "p.id, p.cliente.id, p.cliente.nombreCliente, p.nombreProyecto, p.descripcion, "
                        + "p.direccionPropiedad, p.estado, p.fechaInicio, p.fechaFinEstimada, "
                        + "p.fechaFinalizacionReal, p.empleadoResponsable.id, COALESCE(p.empleadoResponsable.nombreCompleto, 'No asignado'), "
                        + "p.montoContrato, p.montoRecibido, p.fechaUltimoPagoRecibido, "
                        + "p.costoMaterialesConsolidado, p.otrosGastosDirectosConsolidado,p.costoManoDeObra, "
                        + "p.progresoPorcentaje, p.notasProyecto, p.fechaCreacion) "
                        + "FROM Proyecto p LEFT JOIN p.cliente c LEFT JOIN p.empleadoResponsable e WHERE p.empresa.id = :empresaId AND (:filter IS NULL OR "
                        + "LOWER(p.nombreProyecto) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
                        + "LOWER(c.nombreCliente) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
                        + "LOWER(e.nombreCompleto) LIKE LOWER(CONCAT('%', :filter, '%')))")
        Page<ProyectoResponse> findByFilterWithDetails(@Param("empresaId") Long empresaId,
                        @Param("filter") String filter, Pageable pageable);

        /**
         * Consulta para exportación que devuelve la entidad completa, ya
         * que el DTO de exportación necesita el objeto completo para mapear.
         */
        @Query("SELECT p FROM Proyecto p JOIN FETCH p.cliente c LEFT JOIN FETCH p.empleadoResponsable e "
                        + "WHERE p.empresa.id = :empresaId AND (:filter IS NULL OR "
                        + "LOWER(p.nombreProyecto) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
                        + "LOWER(c.nombreCliente) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
                        + "LOWER(e.nombreCompleto) LIKE LOWER(CONCAT('%', :filter, '%')))")
        List<Proyecto> findByFilterForExport(@Param("empresaId") Long empresaId, @Param("filter") String filter,
                        Sort sort);

        /* ======================================================================= */
        /* MÉTODOS EXCLUSIVOS PARA DASHBOARDSERVICE */
        /* ======================================================================= */
        @Query("SELECT DISTINCT YEAR(p.fechaInicio) FROM Proyecto p WHERE p.empresa.id = :empresaId ORDER BY YEAR(p.fechaInicio) DESC")
        List<Integer> findDistinctYearsByEmpresaId(@Param("empresaId") Long empresaId);

        @Query("SELECT p.id, p.nombreProyecto FROM Proyecto p WHERE p.empresa.id = :empresaId AND "
                        + "YEAR(p.fechaInicio) = :year AND (:month is null OR MONTH(p.fechaInicio) = :month)")
        List<Object[]> findProyectosByYearAndMonth(@Param("empresaId") Long empresaId, @Param("year") Integer year,
                        @Param("month") Integer month);

        // --- Consultas filtradas por AÑO (Corregidas con empresaId) ---
        @Query("SELECT SUM(p.costoManoDeObra) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year")
        BigDecimal sumCostoManoDeObraByYear(@Param("empresaId") Long empresaId, @Param("year") int year);

        @Query("SELECT COUNT(p) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year")
        Long countByYear(@Param("empresaId") Long empresaId, @Param("year") int year);

        @Query("SELECT SUM(p.montoRecibido) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year")
        BigDecimal sumMontoRecibidoByYear(@Param("empresaId") Long empresaId, @Param("year") int year);

        @Query("SELECT SUM(p.costoMaterialesConsolidado) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year")
        BigDecimal sumCostoMaterialesConsolidadoByYear(@Param("empresaId") Long empresaId, @Param("year") int year);

        @Query("SELECT SUM(p.otrosGastosDirectosConsolidado) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year")
        BigDecimal sumOtrosGastosDirectosConsolidadoByYear(@Param("empresaId") Long empresaId, @Param("year") int year);

        @Query("SELECT p.estado, COUNT(p) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year GROUP BY p.estado")
        List<Object[]> countProyectosByEstadoByYear(@Param("empresaId") Long empresaId, @Param("year") int year);

        // --- Consultas filtradas por AÑO Y MES (Corregidas con empresaId) ---
        @Query("SELECT SUM(p.costoManoDeObra) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year AND MONTH(p.fechaInicio) = :month")
        BigDecimal sumCostoManoDeObraByYearAndMonth(@Param("empresaId") Long empresaId, @Param("year") int year,
                        @Param("month") int month);

        @Query("SELECT COUNT(p) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year AND MONTH(p.fechaInicio) = :month")
        Long countByYearAndMonth(@Param("empresaId") Long empresaId, @Param("year") int year,
                        @Param("month") int month);

        @Query("SELECT SUM(p.montoRecibido) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year AND MONTH(p.fechaInicio) = :month")
        BigDecimal sumMontoRecibidoByYearAndMonth(@Param("empresaId") Long empresaId, @Param("year") int year,
                        @Param("month") int month);

        @Query("SELECT SUM(p.costoMaterialesConsolidado) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year AND MONTH(p.fechaInicio) = :month")
        BigDecimal sumCostoMaterialesConsolidadoByYearAndMonth(@Param("empresaId") Long empresaId,
                        @Param("year") int year, @Param("month") int month);

        @Query("SELECT SUM(p.otrosGastosDirectosConsolidado) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year AND MONTH(p.fechaInicio) = :month")
        BigDecimal sumOtrosGastosDirectosConsolidadoByYearAndMonth(@Param("empresaId") Long empresaId,
                        @Param("year") int year, @Param("month") int month);

        @Query("SELECT p.estado, COUNT(p) FROM Proyecto p WHERE p.empresa.id = :empresaId AND YEAR(p.fechaInicio) = :year AND MONTH(p.fechaInicio) = :month GROUP BY p.estado")
        List<Object[]> countProyectosByEstadoByYearAndMonth(@Param("empresaId") Long empresaId, @Param("year") int year,
                        @Param("month") int month);

        // --- Consultas filtradas por ID de Proyecto (Corregidas con empresaId para
        // doble seguridad) ---
        @Query("SELECT SUM(p.costoManoDeObra) FROM Proyecto p WHERE p.id = :projectId AND p.empresa.id = :empresaId")
        BigDecimal sumCostoManoDeObraByProjectId(@Param("projectId") Long projectId,
                        @Param("empresaId") Long empresaId);

        @Query("SELECT SUM(p.montoRecibido) FROM Proyecto p WHERE p.id = :projectId AND p.empresa.id = :empresaId")
        BigDecimal sumMontoRecibidoByProjectId(@Param("projectId") Long projectId, @Param("empresaId") Long empresaId);

        @Query("SELECT SUM(p.costoMaterialesConsolidado) FROM Proyecto p WHERE p.id = :projectId AND p.empresa.id = :empresaId")
        BigDecimal sumCostoMaterialesConsolidadoByProjectId(@Param("projectId") Long projectId,
                        @Param("empresaId") Long empresaId);

        @Query("SELECT SUM(p.otrosGastosDirectosConsolidado) FROM Proyecto p WHERE p.id = :projectId AND p.empresa.id = :empresaId")
        BigDecimal sumOtrosGastosDirectosConsolidadoByProjectId(@Param("projectId") Long projectId,
                        @Param("empresaId") Long empresaId);

        @Query("SELECT p.estado, COUNT(p) FROM Proyecto p WHERE p.id = :projectId AND p.empresa.id = :empresaId GROUP BY p.estado")
        List<Object[]> countProyectosByEstadoByProjectId(@Param("projectId") Long projectId,
                        @Param("empresaId") Long empresaId);

}
