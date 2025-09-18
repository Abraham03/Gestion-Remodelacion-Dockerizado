package com.gestionremodelacion.gestion.empresa.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestionremodelacion.gestion.empresa.model.Empresa.EstadoSuscripcion;
import com.gestionremodelacion.gestion.empresa.model.Empresa.PlanSuscripcion;

public class EmpresaResponse {
    private Long id;
    private String nombreEmpresa;
    private boolean activo;
    private PlanSuscripcion plan;
    private EstadoSuscripcion estadoSuscripcion;
    private LocalDate fechaInicioSuscripcion;
    private LocalDate fechaFinSuscripcion;
    private String idClientePago;
    private LocalDateTime trialTerminaEn;
    private String logoUrl;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String telefono;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

}
