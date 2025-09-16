package com.gestionremodelacion.gestion.empresa.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestionremodelacion.gestion.empresa.model.Empresa.EstadoSuscripcion;
import com.gestionremodelacion.gestion.empresa.model.Empresa.PlanSuscripcion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EmpresaRequest {
    @NotBlank(message = "El nombre de la empresa no puede estar vacío")
    @Size(min = 2, max = 100)
    private String nombreEmpresa;

    private boolean activo = true;

    @NotNull(message = "El plan de suscripción es obligatorio")
    private PlanSuscripcion plan;

    @NotNull(message = "El estado de la suscripción es obligatorio")
    private EstadoSuscripcion estadoSuscripcion;

    private LocalDate fechaInicioSuscripcion;
    private LocalDate fechaFinSuscripcion;
    private String idClientePago;
    private LocalDateTime trialTerminaEn;
    private String logoUrl;

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public PlanSuscripcion getPlan() {
        return plan;
    }

    public void setPlan(PlanSuscripcion plan) {
        this.plan = plan;
    }

    public EstadoSuscripcion getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public void setEstadoSuscripcion(EstadoSuscripcion estadoSuscripcion) {
        this.estadoSuscripcion = estadoSuscripcion;
    }

    public LocalDate getFechaInicioSuscripcion() {
        return fechaInicioSuscripcion;
    }

    public void setFechaInicioSuscripcion(LocalDate fechaInicioSuscripcion) {
        this.fechaInicioSuscripcion = fechaInicioSuscripcion;
    }

    public LocalDate getFechaFinSuscripcion() {
        return fechaFinSuscripcion;
    }

    public void setFechaFinSuscripcion(LocalDate fechaFinSuscripcion) {
        this.fechaFinSuscripcion = fechaFinSuscripcion;
    }

    public String getIdClientePago() {
        return idClientePago;
    }

    public void setIdClientePago(String idClientePago) {
        this.idClientePago = idClientePago;
    }

    public LocalDateTime getTrialTerminaEn() {
        return trialTerminaEn;
    }

    public void setTrialTerminaEn(LocalDateTime trialTerminaEn) {
        this.trialTerminaEn = trialTerminaEn;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

}
