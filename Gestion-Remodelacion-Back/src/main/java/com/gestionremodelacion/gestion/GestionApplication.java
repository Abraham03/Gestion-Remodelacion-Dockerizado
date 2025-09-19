package com.gestionremodelacion.gestion;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.context.support.StandardServletEnvironment;

@SpringBootApplication(exclude = { SpringDocConfiguration.class }) // <-- MODIFICA ESTA LÍNEA
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)

public class GestionApplication {

    public static void main(String[] args) {
        // Esta configuración es la que fuerza el inicio como un servidor web y lo mantiene activo
        new SpringApplicationBuilder(GestionApplication.class)
                .environment(new StandardServletEnvironment())
                .run(args);
    }

}
