package com.gestionremodelacion.gestion.proyecto.dto.response;

public class ProyectoDropdownResponse {

    private Long id;
    private String nombreProyecto;

    // Constructor
    public ProyectoDropdownResponse(Long id, String nombreProyecto) {
        this.id = id;
        this.nombreProyecto = nombreProyecto;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

}
