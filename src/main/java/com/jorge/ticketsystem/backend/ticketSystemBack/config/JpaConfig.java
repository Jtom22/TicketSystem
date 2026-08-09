package com.jorge.ticketsystem.backend.ticketSystemBack.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    //   @Bean
    // public AuditorAware<String> auditorProvider() {

        
    //     // En un proyecto real, aquí buscarías el usuario en Spring Security.
    //     // Por ahora, para probar, puedes devolver un nombre fijo o simularlo:
    //     return () -> Optional.of("SISTEMA_SPRING"); 
    // }

    //Con esta clase rellenamos la auditoria de las entidades en la bbdd
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Si no hay autenticación, no está autenticado o es anónimo (ej: durante el registro o login)
            if (authentication == null || 
                !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("SYSTEM"); // Usuario por defecto para acciones no autenticadas
            }

            // Devuelve el identificador del usuario (el email o username guardado en el JWT)
            return Optional.of(authentication.getName());
        };
    }
}
