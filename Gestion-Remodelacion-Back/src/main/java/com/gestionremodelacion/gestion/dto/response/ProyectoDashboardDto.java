package com.gestionremodelacion.gestion.dto.response;

import java.time.LocalDate;

public class ProyectoDashboardDto {

    private Long id;
    private String nombreProyecto;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private String estado;
    private Integer progresoPorcentaje; // <--- CHANGE THIS FIELD TO 'int'

    public ProyectoDashboardDto(
            Long id,
            String nombreProyecto,
            String descripcion,
            LocalDate fechaInicio,
            LocalDate fechaFinEstimada,
            String estado,
            Integer progresoPorcentaje) { // <--- CHANGE THIS PARAMETER TO 'int'

        this.id = id;
        this.nombreProyecto = nombreProyecto;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
        this.estado = estado;
        this.progresoPorcentaje = progresoPorcentaje;
    }

    // --- Ensure Getters and Setters also reflect the primitive 'int' type ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFinEstimada() {
        return fechaFinEstimada;
    }

    public void setFechaFinEstimada(LocalDate fechaFinEstimada) {
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getProgresoPorcentaje() { // <--- CHANGE THIS GETTER TO 'int'
        return progresoPorcentaje;
    }

    public void setProgresoPorcentaje(Integer progresoPorcentaje) { // <--- CHANGE THIS SETTER TO 'int'
        if (progresoPorcentaje < 0) {
            this.progresoPorcentaje = 0;
        } else if (progresoPorcentaje > 100) {
            this.progresoPorcentaje = 100;
        } else {
            this.progresoPorcentaje = progresoPorcentaje;
        }
    }
}
