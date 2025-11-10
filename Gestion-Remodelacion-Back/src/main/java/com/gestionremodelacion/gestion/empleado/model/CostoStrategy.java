package com.gestionremodelacion.gestion.empleado.model;
// package com.gestionremodelacion.gestion.empleado.model;

import java.math.BigDecimal;

/**
 * Interfaz para el Patrón Strategy. Define el contrato para
 * calcular costos basados en el modelo de pago.
 */
public interface CostoStrategy {

    /**
     * Convierte un monto de "display" (ej. 800 por día) al costo
     * base (costo por hora) para guardarlo en la BD.
     *
     * @param montoDisplay El valor del formulario (ej. 800).
     * @return El costo por hora real (ej. 100).
     */
    BigDecimal calcularCostoBasePorHora(BigDecimal montoDisplay);

    /**
     * Convierte el costo base (costo por hora) de la BD
     * al monto de "display" (ej. 800 por día).
     *
     * @param costoBasePorHora El valor de 'costoPorHora' en la BD (ej. 100).
     * @return El monto a mostrar en el DTO (ej. 800).
     */
    BigDecimal calcularMontoDisplay(BigDecimal costoBasePorHora);
}