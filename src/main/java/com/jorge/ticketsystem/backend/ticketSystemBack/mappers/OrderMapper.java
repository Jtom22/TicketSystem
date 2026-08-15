package com.jorge.ticketsystem.backend.ticketSystemBack.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Order;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.id", source = "userId") // Mapea el Long userId al id del objeto User interno
    // @Mapping(target = "total_amount", source = "totalAmount") // Traduce camelCase a snake_case
    @Mapping(target = "status", ignore = true) // Lo fija el service: siempre nace como Pending
    @Mapping(target = "expires_at", ignore = true)//Lo ignoramos para evitar inyecciones de tiempo maliciosas
    Order toEntity(OrderCreateDto dto);

    // 2. De Entidad a DTO de Respuesta (GETs)
    @Mapping(target = "userId", source = "user.id") // Extrae el id del objeto User hacia el Long del DTO
    @Mapping(target = "totalAmount", source = "total_amount")
    @Mapping(target = "expiresAt", source = "expires_at")
    OrderResponseDto toResponseDto(Order entity);

    // 3. Actualización parcial (PATCH/PUT)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // Mantiene el User original de la base de datos intacto
    @Mapping(target = "total_amount", source = "totalAmount")
    @Mapping(target = "expires_at", source = "expiresAt")
    void updateEntityFromDto(OrderUpdateDto dto, @MappingTarget Order entity);

}
