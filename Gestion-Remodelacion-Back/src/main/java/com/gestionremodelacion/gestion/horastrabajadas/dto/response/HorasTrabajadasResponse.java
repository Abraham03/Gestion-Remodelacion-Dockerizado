package com.gestionremodelacion.gestion.horastrabajadas.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestionremodelacion.gestion.empleado.model.Empleado;
import com.gestionremodelacion.gestion.empleado.model.ModeloDePago;

public class HorasTrabajadasResponse {

    private Long id;
    private Long idEmpleado;
    private String nombreEmpleado;
    private Long idProyecto;
    private String nombreProyecto;
    private LocalDate fecha;
    private BigDecimal horas;
    private String cantidad;
    private String unidad;
    private BigDecimal costoPorHoraActual;
    private String actividadRealizada;
    private LocalDateTime fechaRegistro;
    private BigDecimal montoTotal;

    public HorasTrabajadasResponse(Long id, Long idEmpleado, String nombreEmpleado, Empleado empleado, Long idProyecto,
            String nombreProyecto, LocalDate fecha, BigDecimal horas, BigDecimal costoPorHoraActual,
            String actividadRealizada,
            LocalDateTime fechaRegistro) { // Constructor con LocalDate y LocalDateTime
        this.id = id;
        this.idEmpleado = idEmpleado;
        this.nombreEmpleado = nombreEmpleado;
        this.idProyecto = idProyecto;
        this.nombreProyecto = nombreProyecto;
        this.fecha = fecha;
        this.horas = horas;
        this.costoPorHoraActual = costoPorHoraActual;
        this.actividadRealizada = actividadRealizada;
        this.fechaRegistro = fechaRegistro;

        // Se usa un formateador para eliminar los decimales innecesarios.
        DecimalFormat df = new DecimalFormat("0.##");
        // Calculamos el monto total
        this.montoTotal = (horas != null && costoPorHoraActual != null) ? horas.multiply(costoPorHoraActual)
                : BigDecimal.ZERO;

        // LÓGICA PARA DETERMINAR CANTIDAD Y UNIDAD
        if (empleado != null && empleado.getModeloDePago() == ModeloDePago.POR_DIA) {
            this.unidad = "Días";
            BigDecimal dias = horas.divide(new BigDecimal("8"), 2, RoundingMode.HALF_UP);
            this.cantidad = df.format(dias); // <-- Se aplica el formato
        } else {
            this.unidad = "Horas";
            this.cantidad = df.format(horas); // <-- Se aplica el formato
        }

    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public Long getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(Long idProyecto) {
        this.idProyecto = idProyecto;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public LocalDate getFecha() { // Getter para LocalDate
        return fecha;
    }

    public void setFecha(LocalDate fecha) { // Setter para LocalDate
        this.fecha = fecha;
    }

    public BigDecimal getHoras() {
        return horas;
    }

    public void setHoras(BigDecimal horas) {
        this.horas = horas;
    }

    public BigDecimal getCostoPorHoraActual() {
        return costoPorHoraActual;
    }

    public void setCostoPorHoraActual(BigDecimal costoPorHoraActual) {
        this.costoPorHoraActual = costoPorHoraActual;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getActividadRealizada() {
        return actividadRealizada;
    }

    public void setActividadRealizada(String actividadRealizada) {
        this.actividadRealizada = actividadRealizada;
    }

    public LocalDateTime getFechaRegistro() { // Getter para LocalDateTime
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) { // Setter para LocalDateTime
        this.fechaRegistro = fechaRegistro;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
}
