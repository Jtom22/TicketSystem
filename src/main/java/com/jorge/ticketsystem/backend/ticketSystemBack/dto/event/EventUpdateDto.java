package com.jorge.ticketsystem.backend.ticketSystemBack.dto.event;



import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record EventUpdateDto(
    @Size(min = 3, max = 150, message = "El título debe tener entre 3 y 150 caracteres")
    String title,

    String artist,

    String venueName,

    String city,

    @Future(message = "La nueva fecha del evento debe ser en el futuro")
    LocalDateTime eventDate
) {}
