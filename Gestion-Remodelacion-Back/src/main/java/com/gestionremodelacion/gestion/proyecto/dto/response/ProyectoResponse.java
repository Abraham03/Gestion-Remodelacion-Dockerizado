package com.gestionremodelacion.gestion.proyecto.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestionremodelacion.gestion.proyecto.model.Proyecto;

public class ProyectoResponse {

    private Long id;
    private Long idCliente;
    private String nombreCliente; // Para mostrar el nombre del cliente en el DTO
    private String nombreProyecto;
    private String descripcion;
    private String direccionPropiedad;
    private Proyecto.EstadoProyecto estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private LocalDate fechaFinalizacionReal;
    private Long idEmpleadoResponsable;
    private String nombreEmpleadoResponsable; // Para mostrar el nombre del empleado
    private BigDecimal montoContrato;
    private BigDecimal montoRecibido;
    private LocalDate fechaUltimoPagoRecibido;
    private BigDecimal costoMaterialesConsolidado;
    private BigDecimal otrosGastosDirectosConsolidado;
    private Integer progresoPorcentaje;
    private String notasProyecto;
    private LocalDateTime fechaCreacion;

    public ProyectoResponse(Long id, Long idCliente, String nombreCliente, String nombreProyecto, String descripcion,
            String direccionPropiedad, Proyecto.EstadoProyecto estado, LocalDate fechaInicio,
            LocalDate fechaFinEstimada, LocalDate fechaFinalizacionReal, Long idEmpleadoResponsable,
            String nombreEmpleadoResponsable, BigDecimal montoContrato, BigDecimal montoRecibido,
            LocalDate fechaUltimoPagoRecibido, BigDecimal costoMaterialesConsolidado,
            BigDecimal otrosGastosDirectosConsolidado, Integer progresoPorcentaje, String notasProyecto,
            LocalDateTime fechaCreacion) {
        this.id = id;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.nombreProyecto = nombreProyecto;
        this.descripcion = descripcion;
        this.direccionPropiedad = direccionPropiedad;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
        this.fechaFinalizacionReal = fechaFinalizacionReal;
        this.idEmpleadoResponsable = idEmpleadoResponsable;
        this.nombreEmpleadoResponsable = nombreEmpleadoResponsable;
        this.montoContrato = montoContrato;
        this.montoRecibido = montoRecibido;
        this.fechaUltimoPagoRecibido = fechaUltimoPagoRecibido;
        this.costoMaterialesConsolidado = costoMaterialesConsolidado;
        this.otrosGastosDirectosConsolidado = otrosGastosDirectosConsolidado;
        this.progresoPorcentaje = progresoPorcentaje;
        this.notasProyecto = notasProyecto;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
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

    public String getNombreEmpleadoResponsable() {
        return nombreEmpleadoResponsable;
    }

    public void setNombreEmpleadoResponsable(String nombreEmpleadoResponsable) {
        this.nombreEmpleadoResponsable = nombreEmpleadoResponsable;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
