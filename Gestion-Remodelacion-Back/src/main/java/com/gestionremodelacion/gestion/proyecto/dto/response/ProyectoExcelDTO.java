package com.gestionremodelacion.gestion.proyecto.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.gestionremodelacion.gestion.export.Exportable;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto.EstadoProyecto;
import com.gestionremodelacion.gestion.util.FormatUtils;

public class ProyectoExcelDTO implements Exportable {

    private final Proyecto proyecto;

    public ProyectoExcelDTO(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    @Override
    public List<String> getExportHeaders() {
        return Arrays.asList(
                "Nombre Proyecto", "Cliente", "Responsable", "Estado", "Progreso (%)",
                "Monto Contrato", "Monto Recibido", "Costo Materiales", "Otros Gastos Directos", "Costo Mano de Obra",
                "Fecha Inicio", "Fecha Fin Estimada",
                "Fecha Finalización Real", "Dirección", "Notas");
    }

    @Override
    public List<List<String>> getExportData() {
        // Helper para formatear fechas de forma segura
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaInicio = proyecto.getFechaInicio() != null ? proyecto.getFechaInicio().format(formatter) : "N/A";
        String fechaFinEstimada = proyecto.getFechaFinEstimada() != null
                ? proyecto.getFechaFinEstimada().format(formatter)
                : "N/A";
        String fechaFinalizacionReal = proyecto.getFechaFinalizacionReal() != null
                ? proyecto.getFechaFinalizacionReal().format(formatter)
                : "N/A";
        // Nombres de entidades relacionadas, en caso de no tenerlos, se muestra "N/A"
        String clienteNombre = proyecto.getCliente() != null ? proyecto.getCliente().getNombreCliente() : "N/A";
        String empleadoNombre = proyecto.getEmpleadoResponsable() != null
                ? proyecto.getEmpleadoResponsable().getNombreCompleto()
                : "N/A";

        // 2. Se utilizan los nuevos métodos de formato
        String estado = formatEstado(proyecto.getEstado());
        String progreso = formatPorcentaje(proyecto.getProgresoPorcentaje());

        // Se usa la nueva clase de utilidad para todos los campos de moneda
        String montoContrato = FormatUtils.formatCurrency(proyecto.getMontoContrato());
        String montoRecibido = FormatUtils.formatCurrency(proyecto.getMontoRecibido());
        String costoMateriales = FormatUtils.formatCurrency(proyecto.getCostoMaterialesConsolidado());
        String otrosGastos = FormatUtils.formatCurrency(proyecto.getOtrosGastosDirectosConsolidado());
        String costoManoDeObra = FormatUtils.formatCurrency(proyecto.getCostoManoDeObra());

        return Arrays.asList(Arrays.asList(
                proyecto.getNombreProyecto(),
                clienteNombre,
                empleadoNombre,
                estado,
                progreso,
                montoContrato,
                montoRecibido,
                costoMateriales,
                otrosGastos,
                costoManoDeObra,
                fechaInicio,
                fechaFinEstimada,
                fechaFinalizacionReal,
                proyecto.getDireccionPropiedad(),
                proyecto.getNotasProyecto()));
    }

    private String formatPorcentaje(Integer value) {
        if (value == null) {
            return "0%";
        }
        return value + "%";
    }

    private String formatEstado(EstadoProyecto estado) {
        if (estado == null) {
            return "N/A";
        }
        switch (estado) {
            case PENDIENTE -> {
                return "Pendiente";
            }
            case EN_PROGRESO -> {
                return "En Progreso";
            }
            case EN_PAUSA -> {
                return "En Pausa";
            }
            case FINALIZADO -> {
                return "Finalizado";
            }
            case CANCELADO -> {
                return "Cancelado";
            }
            default -> {
                // Capitaliza el nombre del enum como fallback (ej. MI_NUEVO_ESTADO ->
                // Mi_nuevo_estado)
                String name = estado.name().replace("_", " ").toLowerCase();
                return name.substring(0, 1).toUpperCase() + name.substring(1);
            }
        }
    }

}
