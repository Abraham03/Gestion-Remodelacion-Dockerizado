package com.gestionremodelacion.gestion.empresa.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "Empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Información Básica de la Empresa ---

    @Column(name = "nombre_empresa", nullable = false, unique = true)
    private String nombreEmpresa; // Nombre comercial del cliente que te paga.

    @Column(name = "activo", nullable = false)
    private boolean activo = true; // Permite desactivar una cuenta sin borrarla.

    // --- Gestión de Suscripción y Planes ---

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private PlanSuscripcion plan; // El plan actual al que está suscrita la empresa.

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_suscripcion", nullable = false)
    private EstadoSuscripcion estadoSuscripcion; // Estado actual del pago/suscripción.

    @Column(name = "fecha_inicio_suscripcion")
    private LocalDate fechaInicioSuscripcion; // Cuándo comenzó el período de suscripción actual.

    @Column(name = "fecha_fin_suscripcion")
    private LocalDate fechaFinSuscripcion; // Cuándo termina el período de pago actual y se debe renovar.

    // --- Integración con Pasarela de Pago (MUY IMPORTANTE) ---

    /**
     * ID del cliente en la pasarela de pagos (ej. Stripe, PayPal, MercadoPago).
     * NO guardes números de tarjeta de crédito. Guarda solo este identificador que
     * te da el proveedor de pagos.
     * Con este ID, puedes gestionar suscripciones y cobros a través de la API de la
     * pasarela.
     */
    @Column(name = "id_cliente_pago", unique = true)
    private String idClientePago; // Ejemplo: "cus_Abc123Xyz" de Stripe.

    /**
     * Almacena la fecha y hora en que termina el período de prueba (trial).
     * Puede ser nulo si la empresa nunca tuvo un período de prueba o si ya terminó.
     */
    @Column(name = "trial_termina_en")
    private LocalDateTime trialTerminaEn;

    // --- Campos de Auditoría (Buenas Prácticas) ---

    /**
     * Registra automáticamente la fecha y hora de creación del registro.
     * `updatable = false` asegura que este campo nunca se modifique después de su
     * creación.
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Registra automáticamente la fecha y hora de la última modificación del
     * registro.
     */
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // ENUM PARA LOS PLANES
    public enum PlanSuscripcion {
        BASICO, // Corresponde al plan "Básico"
        NEGOCIOS, // Corresponde al plan "Negocios"
        PROFESIONAL // Corresponde al plan "Profesional"
    }

    // ENUM PARA EL ESTADO
    public enum EstadoSuscripcion {
        ACTIVA,
        CANCELADA,
        VENCIDA
    }

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

}
