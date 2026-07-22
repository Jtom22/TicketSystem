package com.jorge.ticketsystem.backend.ticketSystemBack.dto.user;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateDto (

    String fullName, // Opcional

    @Size(min = 6, message = "Si se actualiza la contraseña, debe tener al menos 6 caracteres")
    String password, // Opcional

    Boolean enabled, // Opcional (Usamos Boolean objeto para que pueda llegar null si no se envía)

    Long roleId // Opcional

) {}

