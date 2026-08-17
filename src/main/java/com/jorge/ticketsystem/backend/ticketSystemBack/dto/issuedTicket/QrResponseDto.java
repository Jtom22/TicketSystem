package com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket;


import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Order;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Seat;

public record QrResponseDto (
    String mensaje,
    Order orderId,
    Seat seatId

) {}