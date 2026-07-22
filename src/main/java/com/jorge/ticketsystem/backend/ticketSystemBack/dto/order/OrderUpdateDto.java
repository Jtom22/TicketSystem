package com.jorge.ticketsystem.backend.ticketSystemBack.dto.order;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderUpdateDto(

    @Positive(message = "El monto total debe ser mayor a cero")
    Integer totalAmount, // Usamos Integer en vez de int para que valide correctamente el @NotNull

    @NotBlank(message = "El estado no puede estar vacío")
    String status,

    @Future(message = "La fecha de expiración debe ser en el futuro")
    LocalDateTime expiresAt
    //No incluimos el user id porque no queremos que se pueda modificar el usuario asignado desde aqui
) {}
