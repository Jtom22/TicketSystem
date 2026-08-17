package com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Event;

public record IssuedTicketValidationDto(
    String qrCodeToken,
    Event eventoActual
) {}
