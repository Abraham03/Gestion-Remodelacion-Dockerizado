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

    @NotNull(message = "Las horas no pueden ser nulas")
    private BigDecimal horas;

    private String actividadRealizada;

    // Constructors
    public HorasTrabajadasRequest() {
    }

    public HorasTrabajadasRequest(Long idEmpleado, Long idProyecto, LocalDate fecha, BigDecimal horas, String actividadRealizada) {
        this.idEmpleado = idEmpleado;
        this.idProyecto = idProyecto;
        this.fecha = fecha;
        this.horas = horas;
        this.actividadRealizada = actividadRealizada;
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

    public BigDecimal getHoras() {
        return horas;
    }

    public void setHoras(BigDecimal horas) {
        this.horas = horas;
    }

    public String getActividadRealizada() {
        return actividadRealizada;
    }

    public void setActividadRealizada(String actividadRealizada) {
        this.actividadRealizada = actividadRealizada;
    }
}
