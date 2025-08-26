package com.gestionremodelacion.gestion.proyecto.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.gestionremodelacion.gestion.export.Exportable;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto;

public class ProyectoExcelDTO implements Exportable {

    private final Proyecto proyecto;

    public ProyectoExcelDTO(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    @Override
    public List<String> getExportHeaders() {
        return Arrays.asList(
                "Nombre Proyecto", "Cliente", "Responsable", "Estado", "Progreso (%)",
                "Monto Contrato", "Monto Recibido", "Fecha Inicio", "Fecha Fin Estimada",
                "Fecha Finalización Real", "Dirección", "Notas"
        );
    }

    @Override
    public List<List<String>> getExportData() {
        // Helper para formatear fechas de forma segura
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaInicio = proyecto.getFechaInicio() != null ? proyecto.getFechaInicio().format(formatter) : "N/A";
        String fechaFinEstimada = proyecto.getFechaFinEstimada() != null ? proyecto.getFechaFinEstimada().format(formatter) : "N/A";
        String fechaFinalizacionReal = proyecto.getFechaFinalizacionReal() != null ? proyecto.getFechaFinalizacionReal().format(formatter) : "N/A";

        String clienteNombre = proyecto.getCliente() != null ? proyecto.getCliente().getNombreCliente() : "N/A";
        String empleadoNombre = proyecto.getEmpleadoResponsable() != null ? proyecto.getEmpleadoResponsable().getNombreCompleto() : "N/A";

        return Arrays.asList(Arrays.asList(
                proyecto.getNombreProyecto(),
                clienteNombre,
                empleadoNombre,
                proyecto.getEstado().toString(),
                proyecto.getProgresoPorcentaje().toString(),
                proyecto.getMontoContrato().toString(),
                proyecto.getMontoRecibido().toString(),
                fechaInicio,
                fechaFinEstimada,
                fechaFinalizacionReal,
                proyecto.getDireccionPropiedad(),
                proyecto.getNotasProyecto()
        ));
    }
}
