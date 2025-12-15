package com.gestionremodelacion.gestion.util;

import java.math.BigDecimal;
import java.util.Optional;

public class MathUtils {

    public static BigDecimal getOrDefault(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO);
    }

}
