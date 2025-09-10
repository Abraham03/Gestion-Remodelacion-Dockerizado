package com.gestionremodelacion.gestion.empleado.dto.response;

public class EmpleadoDropdownResponse {
    private Long id;
    private String nombre;
    private String modeloDePago;

    // Constructor
    public EmpleadoDropdownResponse(Long id, String nombre, String modeloDePago) {
        this.id = id;
        this.nombre = nombre;
        this.modeloDePago = modeloDePago;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getModeloDePago() {
        return modeloDePago;
    }

    public void setModeloDePago(String modeloDePago) {
        this.modeloDePago = modeloDePago;
    }
}