package com.jorge.ticketsystem.backend.ticketSystemBack.controllers;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.TicketSystemBackApplication;
import com.jorge.ticketsystem.backend.ticketSystemBack.controllers.AuthController;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.auth.LoginRequestDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.auth.RegisterRequestDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.RoleRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.security.CustomUserDetailsService;
import com.jorge.ticketsystem.backend.ticketSystemBack.security.JwtService;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.UserService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TicketSystemBackApplication.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    //Deja esto asi o el test falla
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void login_conCredencialesCorrectas_devuelveTokenYRoles() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto("jorge@test.com", "123456");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("jorge@test.com")
                .password("hash-no-importa-aqui")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(userDetailsService.loadUserByUsername("jorge@test.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.email").value("jorge@test.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        // Confirmamos que sí se intentó autenticar con AuthenticationManager,
        // que es donde vive la comprobación real de la contraseña.
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_conCredencialesIncorrectas_devuelve401() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto("jorge@test.com", "contraseñaMala");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_conEmailConFormatoInvalido_devuelve400() throws Exception {
        // "no-es-un-email" viola @Email de LoginRequestDto — @Valid debe
        // rechazarlo antes de intentar autenticar nada.
        String jsonInvalido = """
                {"email":"no-es-un-email","password":"123456"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_cuandoNoExisteElRolUSER_devuelve404() throws Exception {
        RegisterRequestDto registerDto = new RegisterRequestDto("Jorge Test", "jorge@test.com", "123456");

        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void register_conPasswordDemasiadoCorta_devuelve400() throws Exception {
        // "123" viola @Size(min = 6) de RegisterRequestDto
        String jsonInvalido = """
                {"fullName":"Jorge Test","email":"jorge@test.com","password":"123"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }
}