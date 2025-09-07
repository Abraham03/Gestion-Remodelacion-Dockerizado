package com.gestionremodelacion.gestion.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.gestionremodelacion.gestion.empresa.model.Empresa.PlanSuscripcion;

/**
 * Anotación personalizada para restringir el acceso a un endpoint
 * basándose en el plan de suscripción de la empresa.
 * Se puede aplicar a cualquier método de un controlador.
 */

// @Target(ElementType.METHOD): Especifica que esta anotación solo se puede
// colocar en métodos.
// No podrías ponerla en una clase o en una variable.
@Target(ElementType.METHOD)

// @Retention(RetentionPolicy.RUNTIME): Especifica que la información de esta
// anotación
// debe estar disponible durante la ejecución del programa (en tiempo de
// ejecución).
// Esto es crucial para que nuestro "Aspecto" pueda leerla y actuar en
// consecuencia.
@Retention(RetentionPolicy.RUNTIME)

public @interface RequiresPlan {

    /**
     * Define un array de planes de suscripción que tienen permitido
     * acceder al método anotado.
     * El nombre "value" es una convención en Java que permite una sintaxis más
     * corta.
     * En lugar de escribir @RequiresPlan(value = { ... }), podemos
     * escribir @RequiresPlan({ ... }).
     * 
     * @return Un array de los enums PlanSuscripcion permitidos.
     */
    PlanSuscripcion[] value();
}