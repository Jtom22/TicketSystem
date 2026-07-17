package com.jorge.ticketsystem.backend.ticketSystemBack.mappers;

import org.mapstruct.*;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.TicketCategory;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketCategoryMapper {

    // 1. De DTO de Creación a Entidad (POST)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event.id", source = "eventId") // Mapea el Long id al objeto Event interno
    TicketCategory toEntity(TicketCategoryCreateDto dto);

    // 2. De Entidad a DTO de Respuesta (GETs)
    @Mapping(target = "eventId", source = "event.id") // Extrae el id del objeto Event hacia el Long del DTO
    TicketCategoryResponseDto toResponseDto(TicketCategory entity);

    // 3. Actualización parcial (PATCH/PUT)
    @Mapping(target = "event", ignore = true)// Mantiene el Event original de la base de datos intacto
    void updateEntityFromDto(TicketCategoryUpdateDto dto, @MappingTarget TicketCategory entity);

    
}
