package com.jorge.ticketsystem.backend.ticketSystemBack.mappers;



import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.IssuedTicket;

//Esto primero de abajo es para ignorar campos nulos ydel objeto de origen, dejando intacto el objeto destinatario
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IssuedTicketMapper {

    // 1. De DTO de Creación a Entidad (Para el POST)
    // El ID lo genera la base de datos y las relaciones complejas se manejan en el Service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "qrCodeToken", source = "qrCodeToken")
    @Mapping(target = "order", ignore = true) // Se asigna mediante el ID en el servicio
    @Mapping(target = "seat", ignore = true)  // Se asigna mediante el ID en el servicio
    IssuedTicket toEntity(IssuedTicketCreateDto dto);

    // 2. De Entidad a DTO de Respuesta (Para los GET, PUT, PATCH)
    // Extraemos los IDs de los objetos complejos 'order' y 'seat' hacia variables planas en el DTO
    @Mapping(target = "qrCodeToken", source = "qrCodeToken")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "seatId", source = "seat.id")
    IssuedTicketResponseDto toResponseDto(IssuedTicket entity);

    // 3. Mapeo de listas para endpoints que devuelven colecciones
    // List<IssuedTicketResponseDto> toResponseDtoList(List<IssuedTicket> entities);
    // //Con esto podemos transformar toda la lista a response dto sin hacer el stream en el service

    // 4. Actualización parcial (Para PATCH / PUT)
    @Mapping(target = "qrCodeToken", source = "qrCodeToken")
    @Mapping(target = "order", ignore = true) // Si cambian las relaciones, se gestionan en el Service
    @Mapping(target = "seat", ignore = true)
    void updateEntityFromDto(IssuedTicketUpdateDto dto, @MappingTarget IssuedTicket entity);
}
