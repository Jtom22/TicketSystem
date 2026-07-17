package com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory;



public record TicketCategoryResponseDto(
    
    String name,
    int price,
    int capacity,
    Long eventId
) {}
