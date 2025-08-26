package com.gestionremodelacion.gestion.cliente.dto.response;

import java.time.LocalDateTime;

public class ClienteResponse {

    private Long id;
    private String nombreCliente;
    private String telefonoContacto;
    private String direccion;
    private String notas;
    private LocalDateTime fechaRegistro;

    public ClienteResponse(Long id, String nombreCliente, String telefonoContacto, String direccion, String notas, LocalDateTime fechaRegistro) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.telefonoContacto = telefonoContacto;
        this.direccion = direccion;
        this.notas = notas;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
