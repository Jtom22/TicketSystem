package com.jorge.ticketsystem.backend.ticketSystemBack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.jorge.ticketsystem.backend.ticketSystemBack.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import java.util.List;


@Configuration
@EnableWebSecurity // Activa el soporte de seguridad web de Spring Security
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService; // Interfaz de Spring qpara buscar los datos de usuario
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Para encriptar contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // componente encargado de procesar una autenticación de base de datos.
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    // Para manejar el login. Este Bean te permitirá llamar al método
    // .authenticate() en AuthController (el que procesa /api/v1/auth/login)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    // Filtros de seguridad
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))// Desactiva la protección CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))// Le prohíbe a Spring sesiones Security crear o guardar en el servidor
                .authorizeHttpRequests(auth -> auth // Abre el bloque para empezar a dictar las reglas de qué endpoints
                                                    // requieren permisos y cuáles no

                        // Estas rutas accede cualquiera
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/ticketCategory/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/seats/**").permitAll()

                        // Documentación de la API: Swagger UI + el JSON de OpenAPI que lo alimenta.
                        // Sin esto, ni siquiera podrías CARGAR la página de Swagger sin loguearte
                        // antes.
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**")
                        .permitAll()

                        // Estas rutas accede solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/ticketCategory/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/ticketCategory/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/ticketCategory/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/seats/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/seats/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/seats/**").hasRole("ADMIN")

                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")

                        // Exige que para ver, crear u operar con órdenes y tickets emitidos, el usuario
                        // debe estar autenticado obligatoriamente con un token válido, sin importar el rol que tenga.
                        .requestMatchers("/api/v1/orders/**").authenticated()
                        .requestMatchers("/api/v1/issued-tickets/**").authenticated()

                        .anyRequest().authenticated()// otra URL que se agregue en el futuro y no esté explícitamente
                                                     // listada arriba, requerirá por defecto estar autenticado
                )
                .authenticationProvider(authenticationProvider())// acopla a la seguridad el proveedor de datos de
                                                                 // usuario con BCrypt
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    // Este método previene los bloqueos de seguridad del navegador cuando el
    // Frontend intenta consumir el Backend desde dominios/puertos distintos
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite explícitamente que mi frontend pueda hacer peticiones a esta API
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Para permitir que el frontend envíe cualquier cabecera
        configuration.setAllowedHeaders(List.of("*"));
        // Para permitir credenciales, cookies o cabeceras de autenticación entre el
        // frontend y el backend
        configuration.setAllowCredentials(true);

        // Aplica todas estas reglas CORS anteriores a absolutamente todas las URLs
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}