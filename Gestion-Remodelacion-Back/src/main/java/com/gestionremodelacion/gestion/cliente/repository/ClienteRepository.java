package com.gestionremodelacion.gestion.cliente.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.gestionremodelacion.gestion.cliente.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /* ======================================================================= */
 /* MÉTODOS PARA ClienteService (CRUD, Paginación, etc.)                    */
 /* ======================================================================= */
    Optional<Cliente> findByNombreCliente(String nombreCliente);

    Page<Cliente> findByNombreClienteContainingIgnoreCaseOrTelefonoContactoContainingIgnoreCase(String nombreCliente, String telefonoContacto, Pageable pageable);

    List<Cliente> findByNombreClienteContainingIgnoreCaseOrTelefonoContactoContainingIgnoreCase(String nombreCliente, String telefonoContacto, Sort sort);

    /* ======================================================================= */
 /* MÉTODOS EXCLUSIVOS PARA DashboardService (Agregaciones)                 */
 /* ======================================================================= */
    // Cuenta clientes por mes para un año específico.
    @Query("SELECT YEAR(c.fechaRegistro) AS anio, MONTH(c.fechaRegistro) AS mes, COUNT(c) FROM Cliente c WHERE YEAR(c.fechaRegistro) = :year GROUP BY anio, mes ORDER BY anio, mes")
    List<Object[]> countClientesByMonthForYear(@Param("year") int year);

    // ✅ NUEVO: Cuenta clientes para un mes y año específicos.
    @Query("SELECT COUNT(c) FROM Cliente c WHERE YEAR(c.fechaRegistro) = :year AND MONTH(c.fechaRegistro) = :month")
    Long countByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
