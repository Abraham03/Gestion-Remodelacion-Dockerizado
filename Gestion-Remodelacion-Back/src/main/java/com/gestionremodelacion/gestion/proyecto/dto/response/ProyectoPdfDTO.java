package com.gestionremodelacion.gestion.proyecto.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.gestionremodelacion.gestion.export.Exportable;
import com.gestionremodelacion.gestion.proyecto.model.Proyecto;

public class ProyectoPdfDTO implements Exportable {

    private final Proyecto proyecto;

    public ProyectoPdfDTO(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    @Override
    public List<String> getExportHeaders() {
        return Arrays.asList(
                "Nombre del Proyecto", "Cliente", "Responsable",
                "Estado", "Fecha de Inicio", "Fecha Fin Estimada", "Monto Contrato"
        );
    }

    @Override
    public List<List<String>> getExportData() {
        // Formateamos las fechas y manejamos posibles nulos
        String fechaInicio = proyecto.getFechaInicio() != null
                ? proyecto.getFechaInicio().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";

        String fechaFinEstimada = proyecto.getFechaFinEstimada() != null
                ? proyecto.getFechaFinEstimada().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";

        String clienteNombre = proyecto.getCliente() != null
                ? proyecto.getCliente().getNombreCliente() : "N/A";

        String empleadoNombre = proyecto.getEmpleadoResponsable() != null
                ? proyecto.getEmpleadoResponsable().getNombreCompleto() : "N/A";

        return Arrays.asList(Arrays.asList(
                proyecto.getNombreProyecto(),
                clienteNombre,
                empleadoNombre,
                proyecto.getEstado().toString(),
                fechaInicio,
                fechaFinEstimada,
                proyecto.getMontoContrato().toString()
        ));
    }
}
