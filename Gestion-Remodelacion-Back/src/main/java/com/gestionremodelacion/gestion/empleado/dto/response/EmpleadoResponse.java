package com.gestionremodelacion.gestion.empleado.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmpleadoResponse {

    private Long id;
    private String nombreCompleto;
    private String rolCargo;
    private String telefonoContacto;
    private LocalDate fechaContratacion;
    private BigDecimal costoPorHora;
    private Boolean activo;
    private String notas;
    private LocalDateTime fechaRegistro;

    public EmpleadoResponse(Long id, String nombreCompleto, String rolCargo, String telefonoContacto,
            LocalDate fechaContratacion, BigDecimal costoPorHora,
            Boolean activo, String notas, LocalDateTime fechaRegistro) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.rolCargo = rolCargo;
        this.telefonoContacto = telefonoContacto;
        this.fechaContratacion = fechaContratacion;
        this.costoPorHora = costoPorHora;
        this.activo = activo;
        this.notas = notas;
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRolCargo() {
        return rolCargo;
    }

    public void setRolCargo(String rolCargo) {
        this.rolCargo = rolCargo;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public BigDecimal getCostoPorHora() {
        return costoPorHora;
    }

    public void setCostoPorHora(BigDecimal costoPorHora) {
        this.costoPorHora = costoPorHora;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

}
