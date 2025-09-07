package com.gestionremodelacion.gestion.security.aspects;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.gestionremodelacion.gestion.empresa.model.Empresa;
import com.gestionremodelacion.gestion.empresa.model.Empresa.EstadoSuscripcion;
import com.gestionremodelacion.gestion.empresa.model.Empresa.PlanSuscripcion;
import com.gestionremodelacion.gestion.model.User;
import com.gestionremodelacion.gestion.security.annotations.RequiresPlan;
import com.gestionremodelacion.gestion.service.user.UserService;

/**
 * Un Aspecto que intercepta las llamadas a los métodos anotados
 * con @RequiresPlan
 * para realizar la validación de la suscripción antes de que el método se
 * ejecute.
 */
@Aspect // Declara esta clase como un Aspecto de AOP.
@Component // Permite que Spring detecte y gestione esta clase como un bean.
public class PlanCheckAspect {

    @Autowired
    private UserService userService; // Inyectamos un servicio para obtener los datos del usuario logueado.

    /**
     * Este es el "Advice" (consejo). Se ejecutará ANTES (@Before) de cualquier
     * método
     * que esté anotado con nuestra anotación @RequiresPlan.
     *
     * La expresión "@annotation(requiresPlan)" le dice a Spring:
     * 1. Busca cualquier método que tenga la
     * anotación @com.GestionRemodelacion.gestion.security.annotations.RequiresPlan
     * 2. Cuando encuentres uno, ejecuta este código justo antes.
     * 3. Pasa el objeto de la anotación misma como un parámetro (`requiresPlan`)
     * para que podamos leer sus valores.
     */
    @Before("@annotation(requiresPlan)")
    public void checkPlan(JoinPoint joinPoint, RequiresPlan requiresPlan) {

        // Paso 1: Obtener la información de la empresa del usuario que hace la
        // petición.
        // Asumimos que tienes un método en tu UserService que devuelve el usuario
        // autenticado
        // desde el SecurityContext de Spring.
        User currentUser = userService.getCurrentUser();
        Empresa empresa = currentUser.getEmpresa();

        // Paso 2: Realizar la primera validación crítica: ¿La suscripción está activa?
        // Si el estado no es ACTIVA (podría ser VENCIDA o CANCELADA), se niega el
        // acceso inmediatamente.
        if (empresa.getEstadoSuscripcion() != EstadoSuscripcion.ACTIVA) {
            throw new AccessDeniedException("Tu suscripción no está activa. Por favor, verifica tu método de pago.");
        }

        // Paso 3: Obtener los planes permitidos de la anotación.
        // `requiresPlan.value()` nos devuelve el array que definimos en el controlador,
        // por ejemplo: {PlanSuscripcion.NEGOCIOS, PlanSuscripcion.PROFESIONAL}
        PlanSuscripcion[] planesPermitidos = requiresPlan.value();

        // Paso 4: Obtener el plan actual de la empresa.
        PlanSuscripcion planActual = empresa.getPlan();

        // Paso 5: Verificar si el plan actual del usuario está en la lista de planes
        // permitidos.
        // Usamos Streams de Java para una comprobación limpia y eficiente.
        boolean tienePermiso = Arrays.stream(planesPermitidos)
                .anyMatch(planPermitido -> planPermitido == planActual);

        // Paso 6: Si después de la comprobación, el usuario no tiene permiso, lanzar
        // una excepción.
        // Spring Security capturará esta excepción y devolverá una respuesta HTTP 403
        // Forbidden.
        if (!tienePermiso) {
            throw new AccessDeniedException(
                    "Tu plan actual no permite el acceso a esta funcionalidad. Considera mejorar tu plan.");
        }
    }
}