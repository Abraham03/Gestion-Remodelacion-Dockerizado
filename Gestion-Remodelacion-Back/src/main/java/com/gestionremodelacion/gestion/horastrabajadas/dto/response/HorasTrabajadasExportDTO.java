package com.gestionremodelacion.gestion.horastrabajadas.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.model.ModeloDePago;
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
                                ? horasTrabajadas.getEmpleado().getNombreCompleto()
                                : "N/A";

                String proyectoNombre = horasTrabajadas.getProyecto() != null
                                ? horasTrabajadas.getProyecto().getNombreProyecto()
                                : "N/A";

                // --- Lógica de cálculo y formato ---
                DecimalFormat df = new DecimalFormat("0.##");
                String cantidad;
                String unidad;
                Empleado empleado = horasTrabajadas.getEmpleado();

                // ✅ Se implementa la lógica condicional que solicitaste
                if (empleado != null && empleado.getModeloDePago() == ModeloDePago.POR_DIA) {
                        unidad = "Días";
                        // Se convierten las horas a días (asumiendo 8 horas por día)
                        BigDecimal dias = horasTrabajadas.getHoras().divide(new BigDecimal("8"), 2,
                                        RoundingMode.HALF_UP);
                        cantidad = df.format(dias);
                } else {
                        // Para el pago por hora, se muestra la cantidad de horas directamente
                        unidad = "Horas";
                        cantidad = df.format(horasTrabajadas.getHoras());
                }

                // ✅ Se calcula y formatea el costo total
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
