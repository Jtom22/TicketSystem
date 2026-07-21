package com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IssuedTicketCreateDto(
    @NotBlank(message = "El token del código QR es obligatorio")
    String qrCodeToken,

    @NotNull(message = "El ID de la orden es obligatorio")
    Long orderId,

    @NotNull(message = "El ID del asiento es obligatorio")
    Long seatId
) {}
