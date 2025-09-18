package com.gestionremodelacion.gestion.empleado.dto.response;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.model.ModeloDePago;
import com.gestionremodelacion.gestion.export.Exportable;
import com.gestionremodelacion.gestion.util.FormatUtils;

public class EmpleadoExportDTO implements Exportable {

    private final Empleado empleado;

    public EmpleadoExportDTO(Empleado empleado) {
        this.empleado = empleado;
    }

    @Override
    public List<String> getExportHeaders() {
        return Arrays.asList("Nombre", "Cargo", "Telefono", "Fecha de Contratacion", "Modelo Pago", "Costo",
                "Activo", "Fecha Registro");
    }

    @Override
    public List<List<String>> getExportData() {
        String fechaFormateada = this.empleado.getFechaContratacion() != null
                ? this.empleado.getFechaContratacion().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "";
        String fechaRegistroFormateada = this.empleado.getFechaRegistro() != null
                ? this.empleado.getFechaRegistro().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "";

        String estadoActivo = this.empleado.getActivo() ? "Activo" : "No Activo";

        String telefonoFormateado = FormatUtils.formatPhoneNumber(this.empleado.getTelefonoContacto());

        // Se añade la lógica para calcular el monto de pago.
        BigDecimal montoDePago;
        if (empleado.getModeloDePago() == ModeloDePago.POR_DIA) {
            // Si el pago es por día, se multiplica el costo por hora por 8.
            montoDePago = this.empleado.getCostoPorHora().multiply(new BigDecimal("8"));
        } else {
            // Si es por hora (o cualquier otro caso), se mantiene el costo por hora.
            montoDePago = this.empleado.getCostoPorHora();
        }

        String montoFormateado = FormatUtils.formatCurrency(montoDePago);

        String modeloDePagoFormateado = FormatUtils.formatModeloDePago(this.empleado.getModeloDePago());

        return Arrays.asList(Arrays.asList(
                this.empleado.getNombreCompleto(),
                this.empleado.getRolCargo(),
                telefonoFormateado,
                fechaFormateada,
                modeloDePagoFormateado,
                montoFormateado,
                estadoActivo,
                fechaRegistroFormateada));
    }

}
