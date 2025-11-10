package com.gestionremodelacion.gestion.empleado.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum ModeloDePago implements CostoStrategy {

    POR_HORA {
        @Override
        public BigDecimal calcularCostoBasePorHora(BigDecimal montoDisplay) {
            // Si el modelo es POR_HORA, el monto es el costo base.
            return montoDisplay;
        }

        @Override
        public BigDecimal calcularMontoDisplay(BigDecimal costoBasePorHora) {
            // Si el modelo es POR_HORA, el costo base es el monto.
            return costoBasePorHora;
        }
    },

    POR_DIA {
        /**
         * Constante privada para la jornada laboral.
         * Esta es ahora la ÚNICA fuente de verdad.
         */
        private static final BigDecimal HORAS_JORNADA = new BigDecimal("8");

        @Override
        public BigDecimal calcularCostoBasePorHora(BigDecimal montoDisplay) {
            // Convierte el monto del día (ej. 800) a costo por hora (ej. 100)
            if (montoDisplay == null) {
                return BigDecimal.ZERO;
            }
            return montoDisplay.divide(HORAS_JORNADA, 2, RoundingMode.HALF_UP);
        }

        @Override
        public BigDecimal calcularMontoDisplay(BigDecimal costoBasePorHora) {
            // Convierte el costo por hora (ej. 100) a monto por día (ej. 800)
            if (costoBasePorHora == null) {
                return BigDecimal.ZERO;
            }
            return costoBasePorHora.multiply(HORAS_JORNADA);
        }
    };

    // Aquí podrías añadir más modelos, ej:
    /*
     * POR_SEMANA {
     * private static final BigDecimal HORAS_SEMANA = new BigDecimal("40");
     * 
     * @Override
     * public BigDecimal calcularCostoBasePorHora(BigDecimal montoDisplay) {
     * if (montoDisplay == null) { return BigDecimal.ZERO; }
     * return montoDisplay.divide(HORAS_SEMANA, 2, RoundingMode.HALF_UP);
     * }
     * 
     * @Override
     * public BigDecimal calcularMontoDisplay(BigDecimal costoBasePorHora) {
     * if (costoBasePorHora == null) { return BigDecimal.ZERO; }
     * return costoBasePorHora.multiply(HORAS_SEMANA);
     * }
     * }
     */
}