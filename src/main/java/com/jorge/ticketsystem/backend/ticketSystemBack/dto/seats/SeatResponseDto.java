package com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats;


public record SeatResponseDto(
    String seatNumber,
    String status,
    String version,
    int categoryId 
) {}


