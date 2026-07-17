package com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SeatCreateDto(
    @NotBlank(message = "El número de asiento no puede estar vacío")
    @Size(min = 1, max = 10, message = "El número de asiento debe tener entre 1 y 10 caracteres")
    String seatNumber,

    @NotBlank(message = "El estado del asiento no puede estar vacío")
    String status,

    @NotNull(message = "El ID del evento es obligatorio")
    int categoryId 
) {}