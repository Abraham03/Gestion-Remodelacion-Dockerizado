package com.gestionremodelacion.gestion.service.dashboard.metrics;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.gestionremodelacion.gestion.proyecto.repository.ProyectoRepository;
import com.gestionremodelacion.gestion.util.MathUtils;

@Service
public class FinanceMetricService {

    private final ProyectoRepository proyectoRepository;

    public FinanceMetricService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    public FinancialSummaryDTO calculateFinancials(Long empresaId, int year, Integer month, Long projectId) {
        BigDecimal montoRecibido;
        BigDecimal costoMateriales;
        BigDecimal otrosGastos;
        BigDecimal costoManoDeObra;

        if (projectId != null) {
            montoRecibido = MathUtils.getOrDefault(proyectoRepository.sumMontoRecibidoByProjectId(projectId, empresaId));
            costoMateriales = MathUtils.getOrDefault(proyectoRepository.sumCostoMaterialesConsolidadoByProjectId(projectId, empresaId));
            otrosGastos = MathUtils.getOrDefault(proyectoRepository.sumOtrosGastosDirectosConsolidadoByProjectId(projectId, empresaId));
            costoManoDeObra = MathUtils.getOrDefault(proyectoRepository.sumCostoManoDeObraByProjectId(projectId, empresaId));
        } else if (month != null && month > 0) {
            montoRecibido = MathUtils.getOrDefault(proyectoRepository.sumMontoRecibidoByYearAndMonth(empresaId, year, month));
            costoMateriales = MathUtils.getOrDefault(proyectoRepository.sumCostoMaterialesConsolidadoByYearAndMonth(empresaId, year, month));
            otrosGastos = MathUtils.getOrDefault(proyectoRepository.sumOtrosGastosDirectosConsolidadoByYearAndMonth(empresaId, year, month));
            costoManoDeObra = MathUtils.getOrDefault(proyectoRepository.sumCostoManoDeObraByYearAndMonth(empresaId, year, month));
        } else {
            montoRecibido = MathUtils.getOrDefault(proyectoRepository.sumMontoRecibidoByYear(empresaId, year));
            costoMateriales = MathUtils.getOrDefault(proyectoRepository.sumCostoMaterialesConsolidadoByYear(empresaId, year));
            otrosGastos = MathUtils.getOrDefault(proyectoRepository.sumOtrosGastosDirectosConsolidadoByYear(empresaId, year));
            costoManoDeObra = MathUtils.getOrDefault(proyectoRepository.sumCostoManoDeObraByYear(empresaId, year));
        }

        BigDecimal balance = montoRecibido.subtract(costoMateriales).subtract(otrosGastos).subtract(costoManoDeObra);

        return new FinancialSummaryDTO(balance, montoRecibido, costoMateriales, otrosGastos, costoManoDeObra);
    }

    // DTO Interno (Record de Java 17)
    public record FinancialSummaryDTO(
            BigDecimal balance,
            BigDecimal ingresos,
            BigDecimal materiales,
            BigDecimal otros,
            BigDecimal manoObra
            ) {

    }
}
