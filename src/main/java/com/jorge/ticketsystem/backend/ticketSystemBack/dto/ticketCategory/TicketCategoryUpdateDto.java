package com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TicketCategoryUpdateDto(
    @NotBlank(message = "El nombre es obligatorio")
    String name,
    
    @Min(value = 0, message = "El precio no puede ser negativo")
    int price,
    
    @Min(value = 1, message = "La capacidad debe ser al menos de 1")
    int capacity
    // Nota: Excluimos 'eventId' para evitar mover un ticket a otro evento
) {}
