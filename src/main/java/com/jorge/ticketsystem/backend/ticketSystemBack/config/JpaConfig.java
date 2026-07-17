package com.jorge.ticketsystem.backend.ticketSystemBack.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
      @Bean
    public AuditorAware<String> auditorProvider() {

        
        // En un proyecto real, aquí buscarías el usuario en Spring Security.
        // Por ahora, para probar, puedes devolver un nombre fijo o simularlo:
        return () -> Optional.of("SISTEMA_SPRING"); 
    }

}
