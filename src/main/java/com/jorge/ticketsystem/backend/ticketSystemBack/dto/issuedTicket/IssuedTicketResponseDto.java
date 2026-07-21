package com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket;

public record IssuedTicketResponseDto(
    Long id,
    String qrCodeToken,
    Long orderId,
    Long seatId
) {}
