package com.gestionremodelacion.gestion;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication(exclude = { SpringDocConfiguration.class }) // <-- MODIFICA ESTA LÍNEA
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)

public class GestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionApplication.class, args);
    }

}
