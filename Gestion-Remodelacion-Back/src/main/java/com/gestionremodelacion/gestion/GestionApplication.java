package com.gestionremodelacion.gestion;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication(exclude = { SpringDocConfiguration.class }) // <-- MODIFICA ESTA LÍNEA
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)

public class GestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(PasswordEncoder passwordEncoder) {
        return args -> {
            String rawPassword = "123456789";
            String encodedPassword = passwordEncoder.encode(rawPassword);

            System.out.println("************************************************************");
            System.out.println("HASH GENERADO PARA LA CONTRASEÑA: " + rawPassword);
            System.out.println("COPIA ESTE HASH -> " + encodedPassword);
            System.out.println("************************************************************");
        };
    }

}
