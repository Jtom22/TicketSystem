package com.jorge.ticketsystem.backend.ticketSystemBack.mappers;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Event;
import org.mapstruct.*;

// 'IGNORE' hace que si un campo viene null en el DTO, MapStruct NO toque el valor original de la BD
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    // 1. De DTO de Creación a Entidad (Para el POST)
    @Mapping(target = "venue_name", source = "venueName")
    @Mapping(target = "event_date", source = "eventDate")
    @Mapping(target = "id", ignore = true) // El ID lo genera la base de datos de forma automática
    Event toEntity(EventCreateDto dto);

    // 2. De Entidad a DTO de Respuesta (Para los GET, PUT, PATCH)
    @Mapping(target = "venueName", source = "venue_name")
    @Mapping(target = "eventDate", source = "event_date")
    EventResponseDto toResponseDto(Event entity);

    // Unimos los nombres diferentes de las variables (DTO -> Entidad)
    @Mapping(target = "venue_name", source = "venueName")
    @Mapping(target = "event_date", source = "eventDate")
    void updateEntityFromDto(EventUpdateDto dto, @MappingTarget Event entity);
}
