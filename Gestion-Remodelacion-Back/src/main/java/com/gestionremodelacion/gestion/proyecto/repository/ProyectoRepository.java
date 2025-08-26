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

    List<Proyecto> findByClienteId(Long clienteId);

    List<Proyecto> findByEmpleadoResponsableId(Long empleadoResponsableId);

    Optional<Proyecto> findByNombreProyecto(String nombreProyecto);

    /**
     * ✅ CAMBIO: Consulta optimizada que devuelve directamente el DTO que
     * necesitas. Es más eficiente que traer la entidad completa y luego
     * mapearla.
     */
    @Query("SELECT new com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoResponse("
            + "p.id, p.cliente.id, p.cliente.nombreCliente, p.nombreProyecto, p.descripcion, "
            + "p.direccionPropiedad, p.estado, p.fechaInicio, p.fechaFinEstimada, "
            + "p.fechaFinalizacionReal, p.empleadoResponsable.id, COALESCE(p.empleadoResponsable.nombreCompleto, 'No asignado'), "
            + "p.montoContrato, p.montoRecibido, p.fechaUltimoPagoRecibido, "
            + "p.costoMaterialesConsolidado, p.otrosGastosDirectosConsolidado, "
            + "p.progresoPorcentaje, p.notasProyecto, p.fechaCreacion) "
            + "FROM Proyecto p")
    Page<ProyectoResponse> findAllWithDetails(Pageable pageable);

    /**
     * ✅ NUEVO: Misma consulta optimizada, pero con filtro.
     */
    @Query("SELECT new com.gestionremodelacion.gestion.proyecto.dto.response.ProyectoResponse("
            + "p.id, p.cliente.id, p.cliente.nombreCliente, p.nombreProyecto, p.descripcion, "
            + "p.direccionPropiedad, p.estado, p.fechaInicio, p.fechaFinEstimada, "
            + "p.fechaFinalizacionReal, p.empleadoResponsable.id, COALESCE(p.empleadoResponsable.nombreCompleto, 'No asignado'), "
            + "p.montoContrato, p.montoRecibido, p.fechaUltimoPagoRecibido, "
            + "p.costoMaterialesConsolidado, p.otrosGastosDirectosConsolidado, "
            + "p.progresoPorcentaje, p.notasProyecto, p.fechaCreacion) "
            + "FROM Proyecto p WHERE "
            + "LOWER(p.nombreProyecto) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
            + "LOWER(p.cliente.nombreCliente) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
            + "LOWER(p.empleadoResponsable.nombreCompleto) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<ProyectoResponse> findByFilterWithDetails(@Param("filter") String filter, Pageable pageable);

    /**
     * ✅ NUEVO: Consulta para exportación que devuelve la entidad completa, ya
     * que el DTO de exportación necesita el objeto completo para mapear.
     */
    @Query("SELECT p FROM Proyecto p JOIN FETCH p.cliente c LEFT JOIN FETCH p.empleadoResponsable e WHERE "
            + "LOWER(p.nombreProyecto) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
            + "LOWER(c.nombreCliente) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
            + "LOWER(e.nombreCompleto) LIKE LOWER(CONCAT('%', :filter, '%'))")
    List<Proyecto> findByFilterForExport(@Param("filter") String filter, Sort sort);


    /* ======================================================================= */
 /* MÉTODOS EXCLUSIVOS PARA DASHBOARDSERVICE                                */
 /* ======================================================================= */
    @Query("SELECT DISTINCT YEAR(p.fechaInicio) FROM Proyecto p ORDER BY YEAR(p.fechaInicio) DESC")
    List<Integer> findDistinctYears();

    // -- Consultas filtradas por AÑO --
    @Query("SELECT COUNT(p) FROM Proyecto p WHERE YEAR(p.fechaInicio) = :year")
    Long countByYear(@Param("year") int year);

    @Query("SELECT SUM(p.montoRecibido) FROM Proyecto p WHERE YEAR(p.fechaInicio) = :year")
    BigDecimal sumMontoRecibidoByYear(@Param("year") int year);

    @Query("SELECT SUM(p.costoMaterialesConsolidado) FROM Proyecto p WHERE YEAR(p.fechaInicio) = :year")
    BigDecimal sumCostoMaterialesConsolidadoByYear(@Param("year") int year);

    @Query("SELECT SUM(p.otrosGastosDirectosConsolidado) FROM Proyecto p WHERE YEAR(p.fechaInicio) = :year")
    BigDecimal sumOtrosGastosDirectosConsolidadoByYear(@Param("year") int year);

    @Query("SELECT p.estado, COUNT(p) FROM Proyecto p WHERE YEAR(p.fechaInicio) = :year GROUP BY p.estado")
    List<Object[]> countProyectosByEstadoByYear(@Param("year") int year);

    // -- ✅ NUEVO: Consultas filtradas por AÑO Y MES --
    @Query("SELECT COUNT(p) FROM Proyecto p WHERE YEAR(p.fechaInicio) = :year AND MONTH(p.fechaInicio) = :month")
    Long countByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(p.montoRecibido) FROM Proyecto p WHERE YEAR(p.fechaInicio) = :year AND MONTH(p.fechaInicio) = :month")
    BigDecimal sumMontoRecibidoByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(p.costoMaterialesConsolidado) FROM Proyecto p WHERE YEAR(p.fechaInicio) = :year AND MONTH(p.fechaInicio) = :month")
    BigDecimal sumCostoMaterialesConsolidadoByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(p.otrosGastosDirectosConsolidado) FROM Proyecto p WHERE YEAR(p.fechaInicio) = :year AND MONTH(p.fechaInicio) = :month")
    BigDecimal sumOtrosGastosDirectosConsolidadoByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT p.estado, COUNT(p) FROM Proyecto p WHERE YEAR(p.fechaInicio) = :year AND MONTH(p.fechaInicio) = :month GROUP BY p.estado")
    List<Object[]> countProyectosByEstadoByYearAndMonth(@Param("year") int year, @Param("month") int month);

}
