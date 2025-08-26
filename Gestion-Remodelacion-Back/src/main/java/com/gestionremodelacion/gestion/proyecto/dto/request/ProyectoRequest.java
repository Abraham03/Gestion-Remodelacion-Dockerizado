package com.gestionremodelacion.gestion.proyecto.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import com.gestionremodelacion.gestion.proyecto.model.Proyecto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProyectoRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long idCliente;

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    private String nombreProyecto;

    private String descripcion;

    @NotBlank(message = "La dirección de la propiedad es obligatoria")
    private String direccionPropiedad;

    @NotNull(message = "El estado del proyecto es obligatorio")
    private Proyecto.EstadoProyecto estado;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFinEstimada;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFinalizacionReal;

    private Long idEmpleadoResponsable;

    @NotNull(message = "El monto del contrato es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto del contrato no puede ser negativo")
    private BigDecimal montoContrato;

    @DecimalMin(value = "0.00", message = "El monto recibido no puede ser negativo")
    private BigDecimal montoRecibido;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaUltimoPagoRecibido;

    @DecimalMin(value = "0.00", message = "El costo de materiales no puede ser negativo")
    private BigDecimal costoMaterialesConsolidado;

    @DecimalMin(value = "0.00", message = "Los otros gastos directos no pueden ser negativos")
    private BigDecimal otrosGastosDirectosConsolidado;

    @Min(value = 0, message = "El progreso no puede ser menor a 0")
    @Max(value = 100, message = "El progreso no puede ser mayor a 100")
    private Integer progresoPorcentaje;

    private String notasProyecto;

    public ProyectoRequest() {
        this.estado = Proyecto.EstadoProyecto.PENDIENTE;
        this.montoRecibido = BigDecimal.ZERO;
        this.costoMaterialesConsolidado = BigDecimal.ZERO;
        this.otrosGastosDirectosConsolidado = BigDecimal.ZERO;
        this.progresoPorcentaje = 0;
    }

    // Getters y Setters
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
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

    public String getDireccionPropiedad() {
        return direccionPropiedad;
    }

    public void setDireccionPropiedad(String direccionPropiedad) {
        this.direccionPropiedad = direccionPropiedad;
    }

    public Proyecto.EstadoProyecto getEstado() {
        return estado;
    }

    public void setEstado(Proyecto.EstadoProyecto estado) {
        this.estado = estado;
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

    public LocalDate getFechaFinalizacionReal() {
        return fechaFinalizacionReal;
    }

    public void setFechaFinalizacionReal(LocalDate fechaFinalizacionReal) {
        this.fechaFinalizacionReal = fechaFinalizacionReal;
    }

    public Long getIdEmpleadoResponsable() {
        return idEmpleadoResponsable;
    }

    public void setIdEmpleadoResponsable(Long idEmpleadoResponsable) {
        this.idEmpleadoResponsable = idEmpleadoResponsable;
    }

    public BigDecimal getMontoContrato() {
        return montoContrato;
    }

    public void setMontoContrato(BigDecimal montoContrato) {
        this.montoContrato = montoContrato;
    }

    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(BigDecimal montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    public LocalDate getFechaUltimoPagoRecibido() {
        return fechaUltimoPagoRecibido;
    }

    public void setFechaUltimoPagoRecibido(LocalDate fechaUltimoPagoRecibido) {
        this.fechaUltimoPagoRecibido = fechaUltimoPagoRecibido;
    }

    public BigDecimal getCostoMaterialesConsolidado() {
        return costoMaterialesConsolidado;
    }

    public void setCostoMaterialesConsolidado(BigDecimal costoMaterialesConsolidado) {
        this.costoMaterialesConsolidado = costoMaterialesConsolidado;
    }

    public BigDecimal getOtrosGastosDirectosConsolidado() {
        return otrosGastosDirectosConsolidado;
    }

    public void setOtrosGastosDirectosConsolidado(BigDecimal otrosGastosDirectosConsolidado) {
        this.otrosGastosDirectosConsolidado = otrosGastosDirectosConsolidado;
    }

    public Integer getProgresoPorcentaje() {
        return progresoPorcentaje;
    }

    public void setProgresoPorcentaje(Integer progresoPorcentaje) {
        this.progresoPorcentaje = progresoPorcentaje;
    }

    public String getNotasProyecto() {
        return notasProyecto;
    }

    public void setNotasProyecto(String notasProyecto) {
        this.notasProyecto = notasProyecto;
    }
}
