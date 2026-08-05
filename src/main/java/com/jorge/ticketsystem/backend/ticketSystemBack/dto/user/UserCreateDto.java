package com.jorge.ticketsystem.backend.ticketSystemBack.dto.user;


import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateDto(
     
    @NotBlank(message = "El nombre completo es obligatorio")
    String fullName, // Mapeará a full_name

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String password,

    @NotEmpty(message = "Debes asignar al menos un rol al usuario") // <- Lista no nula y no vacía
    List<Long> roleIds // <- Lista de IDs de roles

) {}

