package com.jorge.ticketsystem.backend.ticketSystemBack.dto.user;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateDto(
     
    @NotBlank(message = "El email no puede estar vacío")
    String email,
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String password,

    @Column(name = "enabled", columnDefinition = "TINYINT(1)")
    boolean enabled

) {}

