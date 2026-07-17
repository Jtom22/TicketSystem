package com.jorge.ticketsystem.backend.ticketSystemBack.dto.event;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record EventCreateDto(
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 150, message = "El título debe tener entre 3 y 150 caracteres")
    String title,

    @NotBlank(message = "El artista es obligatorio")
    String artist,

    @NotBlank(message = "El nombre del lugar es obligatorio")
    String venueName,

    @NotBlank(message = "La ciudad es obligatoria")
    String city,

    @NotNull(message = "La fecha del evento es obligatoria")
    @Future(message = "La fecha del evento debe ser en el futuro") // Evita eventos en el pasado
    LocalDateTime eventDate
) {}
