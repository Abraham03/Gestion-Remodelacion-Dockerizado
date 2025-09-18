package com.gestionremodelacion.gestion.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import com.gestionremodelacion.gestion.empleado.model.ModeloDePago;

/**
 * Clase de utilidad para formatear valores comunes en la aplicación.
 */
public final class FormatUtils {

    // Se hace el constructor privado para que la clase no pueda ser instanciada.
    private FormatUtils() {
    }

    /**
     * Formatea un valor BigDecimal a un string de moneda en formato de EE.UU. (ej.
     * $1,234.50).
     * Si el valor es nulo, devuelve un valor predeterminado como "$0.00".
     *
     * @param value El valor BigDecimal a formatear.
     * @return El string formateado como moneda.
     */
    public static String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "$0.00";
        }
        // Se puede cambiar "en", "US" a "es", "MX" para formato mexicano si se desea.
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        return currencyFormatter.format(value);
    }

    // Método de ayuda para formatear el teléfono
    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.replaceAll("\\D", "").length() != 10) {
            return phoneNumber; // Devuelve el original si no es válido
        }
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        return String.format("(%s) %s-%s",
                digitsOnly.substring(0, 3),
                digitsOnly.substring(3, 6),
                digitsOnly.substring(6, 10));
    }

    public static String formatModeloDePago(ModeloDePago modelo) {
        if (modelo == null) {
            return "N/A";
        }
        return switch (modelo) {
            case POR_DIA -> "Por Día";
            case POR_HORA -> "Por Hora";
            default -> modelo.toString();
        }; // Fallback por si se añade un nuevo modelo
    }
}