package com.jorge.ticketsystem.backend.ticketSystemBack.dto.event;

import java.time.LocalDateTime;

public record EventResponseDto(

    String title,
    String artist,
    String venueName,
    String city,
    LocalDateTime eventDate
) {}
