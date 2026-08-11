package com.jorge.ticketsystem.backend.ticketSystemBack.dto.order;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderCreateDto(
    

    @NotNull(message = "El monto total es obligatorio")
    @Positive(message = "El monto total debe ser mayor a cero")
    Integer totalAmount, // Usamos Integer en vez de int para que valide correctamente el @NotNull

    @NotNull(message = "El ID de usuario es obligatorio")
    Long userId,

    @NotEmpty(message = "Debes seleccionar al menos un asiento")
    List<Long> seatIds

) {


}
