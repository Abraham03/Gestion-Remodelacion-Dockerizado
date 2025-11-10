package com.gestionremodelacion.gestion.horastrabajadas.dto.response;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.gestionremodelacion.gestion.export.Exportable;
import com.gestionremodelacion.gestion.horastrabajadas.model.HorasTrabajadas;
import com.gestionremodelacion.gestion.util.FormatUtils;

public class HorasTrabajadasExportDTO implements Exportable {

        private final HorasTrabajadas horasTrabajadas;

        public HorasTrabajadasExportDTO(HorasTrabajadas horasTrabajadas) {
                this.horasTrabajadas = horasTrabajadas;
        }

        @Override
        public List<String> getExportHeaders() {
                return Arrays.asList(
                                "Empleado", "Proyecto", "Fecha", "Cantidad", "Unidad", "Costo Total",
                                "Actividad Realizada");
        }

        @Override
        public List<List<String>> getExportData() {
                // Formateamos la fecha y manejamos posibles nulos
                String fecha = horasTrabajadas.getFecha() != null
                                ? horasTrabajadas.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                : "N/A";

                String empleadoNombre = horasTrabajadas.getEmpleado() != null
                                ? horasTrabajadas.getNombreEmpleado()
                                : "N/A";

                String proyectoNombre = horasTrabajadas.getProyecto() != null
                                ? horasTrabajadas.getNombreProyecto()
                                : "N/A";

                String unidad = horasTrabajadas.getUnidad() != null
                                ? horasTrabajadas.getUnidad()
                                : "N/A";

                // Lógica de formato
                DecimalFormat df = new DecimalFormat("0.##");
                String cantidad = horasTrabajadas.getCantidad() != null
                                ? df.format(horasTrabajadas.getCantidad())
                                : "0";

                // Se calcula y formatea el costo total
                BigDecimal costoTotal = BigDecimal.ZERO;
                if (horasTrabajadas.getCostoPorHoraActual() != null && horasTrabajadas.getHoras() != null) {
                        costoTotal = horasTrabajadas.getCostoPorHoraActual().multiply(horasTrabajadas.getHoras());
                }
                String montoFormateado = FormatUtils.formatCurrency(costoTotal);

                return Arrays.asList(Arrays.asList(
                                empleadoNombre,
                                proyectoNombre,
                                fecha,
                                cantidad,
                                unidad,
                                montoFormateado,
                                horasTrabajadas.getActividadRealizada()));

        }
}
