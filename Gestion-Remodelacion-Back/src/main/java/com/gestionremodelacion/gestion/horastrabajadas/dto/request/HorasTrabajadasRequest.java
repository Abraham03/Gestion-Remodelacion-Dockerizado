package com.gestionremodelacion.gestion.horastrabajadas.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull; // Make sure this is LocalDate

public class HorasTrabajadasRequest {

    @NotNull(message = "El ID del empleado no puede ser nulo")
    private Long idEmpleado;

    @NotNull(message = "El ID del proyecto no puede ser nulo")
    private Long idProyecto;

    @NotNull(message = "La fecha no puede ser nula")
    private LocalDate fecha; // Ensure this is LocalDate

    private String actividadRealizada;

    @NotNull(message = "La cantidad no puede ser nula")
    private BigDecimal cantidad;

    @NotNull(message = "La unidad no puede ser nula")
    private String unidad;

    // Constructors
    public HorasTrabajadasRequest() {
    }

    public HorasTrabajadasRequest(
            Long idEmpleado, Long idProyecto, LocalDate fecha,
            String actividadRealizada, BigDecimal cantidad, String unidad) {
        this.idEmpleado = idEmpleado;
        this.idProyecto = idProyecto;
        this.fecha = fecha;
        this.actividadRealizada = actividadRealizada;
        this.cantidad = cantidad;
        this.unidad = unidad;
    }

    // Getters y Setters
    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Long getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(Long idProyecto) {
        this.idProyecto = idProyecto;
    }

    public LocalDate getFecha() { // Getter for LocalDate
        return fecha;
    }

    public void setFecha(LocalDate fecha) { // Setter for LocalDate
        this.fecha = fecha;
    }

    public String getActividadRealizada() {
        return actividadRealizada;
    }

    public void setActividadRealizada(String actividadRealizada) {
        this.actividadRealizada = actividadRealizada;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
}
