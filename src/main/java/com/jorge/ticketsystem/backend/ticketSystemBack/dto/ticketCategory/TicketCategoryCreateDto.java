package com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketCategoryCreateDto(
    @NotBlank(message = "El nombre es obligatorio")
    String name,
    
    @Min(value = 0, message = "El precio no puede ser negativo")
    int price,
    
    @Min(value = 1, message = "La capacidad debe ser al menos de 1")
    int capacity,
    
    @NotNull(message = "El ID del evento es obligatorio")
    Long eventId
) {}
