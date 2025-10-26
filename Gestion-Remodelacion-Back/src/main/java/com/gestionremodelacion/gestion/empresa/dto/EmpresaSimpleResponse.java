package com.gestionremodelacion.gestion.empresa.dto;

public class EmpresaSimpleResponse {

    private Long id;
    private String nombreEmpresa;

    public EmpresaSimpleResponse(Long id, String nombreEmpresa) {
        this.id = id;
        this.nombreEmpresa = nombreEmpresa;
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
}
