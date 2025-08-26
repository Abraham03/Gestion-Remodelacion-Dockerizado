package com.gestionremodelacion.gestion.horastrabajadas.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate; // Importar LocalDate
import java.time.LocalDateTime; // Importar LocalDateTime

public class HorasTrabajadasResponse {

    private Long id;
    private Long idEmpleado;
    private String nombreEmpleado; // Para mostrar el nombre del empleado
    private Long idProyecto;
    private String nombreProyecto; // Para mostrar el nombre del proyecto
    private LocalDate fecha; // Cambiado a LocalDate
    private BigDecimal horas;
    private String actividadRealizada;
    private LocalDateTime fechaRegistro; // Cambiado a LocalDateTime

    public HorasTrabajadasResponse(Long id, Long idEmpleado, String nombreEmpleado, Long idProyecto,
            String nombreProyecto, LocalDate fecha, BigDecimal horas, String actividadRealizada,
            LocalDateTime fechaRegistro) { // Constructor con LocalDate y LocalDateTime
        this.id = id;
        this.idEmpleado = idEmpleado;
        this.nombreEmpleado = nombreEmpleado;
        this.idProyecto = idProyecto;
        this.nombreProyecto = nombreProyecto;
        this.fecha = fecha;
        this.horas = horas;
        this.actividadRealizada = actividadRealizada;
        this.fechaRegistro = fechaRegistro;
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
}
