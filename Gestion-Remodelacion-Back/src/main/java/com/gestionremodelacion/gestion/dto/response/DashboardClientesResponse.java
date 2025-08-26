package com.gestionremodelacion.gestion.dto.response;

import java.util.List;

public class DashboardClientesResponse {

    private final List<Object[]> clientesPorMes;

    public DashboardClientesResponse(List<Object[]> clientesPorMes) {
        this.clientesPorMes = clientesPorMes;
    }

    public List<Object[]> getClientesPorMes() {
        return clientesPorMes;
    }
}
