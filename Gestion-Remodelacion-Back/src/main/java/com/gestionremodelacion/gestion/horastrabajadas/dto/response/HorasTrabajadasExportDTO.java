package com.gestionremodelacion.gestion.horastrabajadas.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.gestionremodelacion.gestion.export.Exportable;
import com.gestionremodelacion.gestion.horastrabajadas.model.HorasTrabajadas;

public class HorasTrabajadasExportDTO implements Exportable {

    private final HorasTrabajadas horasTrabajadas;

    public HorasTrabajadasExportDTO(HorasTrabajadas horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }

    @Override
    public List<String> getExportHeaders() {
        return Arrays.asList(
                "Empleado", "Proyecto", "Fecha", "Horas", "Actividad Realizada"
        );
    }

    @Override
    public List<List<String>> getExportData() {
        // Formateamos la fecha y manejamos posibles nulos
        String fecha = horasTrabajadas.getFecha() != null
                ? horasTrabajadas.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";

        String empleadoNombre = horasTrabajadas.getEmpleado() != null
                ? horasTrabajadas.getEmpleado().getNombreCompleto() : "N/A";

        String proyectoNombre = horasTrabajadas.getProyecto() != null
                ? horasTrabajadas.getProyecto().getNombreProyecto() : "N/A";

        return Arrays.asList(Arrays.asList(
                empleadoNombre,
                proyectoNombre,
                fecha,
                horasTrabajadas.getHoras().toString(),
                horasTrabajadas.getActividadRealizada()
        ));
    }
}
