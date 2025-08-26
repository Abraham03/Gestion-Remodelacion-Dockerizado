package com.gestionremodelacion.gestion.empleado.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EmpleadoRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El rol o cargo es obligatorio")
    private String rolCargo;

    private String telefonoContacto;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaContratacion;

    @NotNull(message = "El costo por hora es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo por hora debe ser mayor a 0")
    private BigDecimal costoPorHora;

    private Boolean activo;
    private String notas;

    public EmpleadoRequest() {
        this.activo = true;
    }

    // Getters y Setters
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

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
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
