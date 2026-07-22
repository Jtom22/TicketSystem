package com.jorge.ticketsystem.backend.ticketSystemBack.dto.order;

import java.time.LocalDateTime;

public record OrderResponseDto(

    Long id,
    int totalAmount,
    String status,
    LocalDateTime expiresAt,
    Long userId
    
) {}
