package com.jorge.ticketsystem.backend.ticketSystemBack.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.auth.AuthResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.auth.LoginRequestDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.auth.RegisterRequestDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Role;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.RoleRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.security.CustomUserDetailsService;
import com.jorge.ticketsystem.backend.ticketSystemBack.security.JwtService;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe el rol USER en la base de datos. Créalo antes de registrar usuarios."));

        UserCreateDto createDto = new UserCreateDto(
                dto.fullName(),
                dto.email(),
                dto.password(),
                List.of(userRole.getId())
        );

        userService.createUser(createDto);

        return login(new LoginRequestDto(dto.email(), dto.password()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.email());
        String token = jwtService.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        AuthResponseDto response = new AuthResponseDto(token, dto.email(), null, roles);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}