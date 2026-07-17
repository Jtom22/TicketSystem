package com.jorge.ticketsystem.backend.ticketSystemBack.mappers;

import org.mapstruct.MappingTarget;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Seat;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SeatMapper {
    
//Ponemos el ignore en todo aquello que rellenamos en otro lado ya sea en el Service o no
    // 1. De DTO de Creación a Entidad (POST)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true) // Lo inicializa la BD o JPA
    @Mapping(target = "ticketCategory", ignore = true) // Lo ignoramos para asociarlo en el Service
    @Mapping(target = "seat_number", source = "seatNumber") // Mapeo de nombres diferentes
    Seat toEntity(SeatCreateDto dto);

    // 2. De Entidad a DTO de Respuesta (GETs)
    @Mapping(target = "seatNumber", source = "seat_number")
    SeatResponseDto toResponseDto(Seat entity);

    // 3. Actualización parcial (PATCH/PUT)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true) // La versión no la edita el usuario manualmente
    @Mapping(target = "ticketCategory", ignore = true)
    @Mapping(target = "seat_number", source = "seatNumber")
    void updateEntityFromDto(SeatUpdateDto dto, @MappingTarget Seat entity);

    
}
