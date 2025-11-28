package com.gestionremodelacion.gestion.empleado.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

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

    @NotBlank(message = "El modelo de pago es obligatorio")
    @Pattern(regexp = "^(POR_HORA|POR_DIA)$", message = "El modelo de pago debe ser 'POR_HORA' o 'POR_DIA'")
    private String modeloDePago;

    private Boolean activo;
    private String notas;

    // --- AÑADIR CAMPOS OPCIONALES PARA CREAR LOGIN ---
    private String username;
    private String email;
    private String password;
    private Set<Long> roles; // IDs de los roles para el nuevo usuario

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

    public String getModeloDePago() {
        return modeloDePago;
    }

    public void setModeloDePago(String modeloDePago) {
        this.modeloDePago = modeloDePago;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Long> getRoles() {
        return roles;
    }

    public void setRoles(Set<Long> roles) {
        this.roles = roles;
    }
}
