package com.gestionremodelacion.gestion.proyecto.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestionremodelacion.gestion.cliente.model.Cliente;
import com.gestionremodelacion.gestion.empleado.model.Empleado;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "Proyectos")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "nombre_proyecto", nullable = false)
    private String nombreProyecto;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "direccion_propiedad", nullable = false)
    private String direccionPropiedad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoProyecto estado;

    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.DATE)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin_estimada")
    @Temporal(TemporalType.DATE)
    private LocalDate fechaFinEstimada;

    @Column(name = "fecha_finalizacion_real")
    @Temporal(TemporalType.DATE)
    private LocalDate fechaFinalizacionReal;

    @ManyToOne
    @JoinColumn(name = "id_empleado_responsable")
    private Empleado empleadoResponsable;

    @Column(name = "monto_contrato", nullable = false)
    private BigDecimal montoContrato;

    @Column(name = "monto_recibido", nullable = false)
    private BigDecimal montoRecibido;

    @Column(name = "fecha_ultimo_pago_recibido")
    @Temporal(TemporalType.DATE)
    private LocalDate fechaUltimoPagoRecibido;

    @Column(name = "costo_materiales_consolidado", nullable = false)
    private BigDecimal costoMaterialesConsolidado;

    @Column(name = "otros_gastos_directos_consolidado", nullable = false)
    private BigDecimal otrosGastosDirectosConsolidado;

    @Column(name = "progreso_porcentaje", nullable = false)
    private Integer progresoPorcentaje;

    @Column(name = "notas_proyecto", columnDefinition = "TEXT")
    private String notasProyecto;

    @Column(name = "fecha_creacion", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime fechaCreacion;

    public Proyecto() {
        this.estado = EstadoProyecto.PENDIENTE;
        this.montoRecibido = BigDecimal.ZERO;
        this.costoMaterialesConsolidado = BigDecimal.ZERO;
        this.otrosGastosDirectosConsolidado = BigDecimal.ZERO;
        this.progresoPorcentaje = 0;
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Enum para el estado del proyecto
    public enum EstadoProyecto {
        PENDIENTE,
        EN_PROGRESO,
        EN_PAUSA,
        FINALIZADO,
        CANCELADO
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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

    public EstadoProyecto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProyecto estado) {
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

    public Empleado getEmpleadoResponsable() {
        return empleadoResponsable;
    }

    public void setEmpleadoResponsable(Empleado empleadoResponsable) {
        this.empleadoResponsable = empleadoResponsable;
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
        if (progresoPorcentaje < 0) {
            this.progresoPorcentaje = 0;
        } else if (progresoPorcentaje > 100) {
            this.progresoPorcentaje = 100;
        } else {
            this.progresoPorcentaje = progresoPorcentaje;
        }
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
