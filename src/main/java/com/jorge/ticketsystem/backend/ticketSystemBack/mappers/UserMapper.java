package com.jorge.ticketsystem.backend.ticketSystemBack.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.User;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    // 1. De DTO de Creación a Entidad (POST)

    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role.id", source = "roleId")
    @Mapping(target = "full_name", source = "fullName")
    @Mapping(target = "enabled", constant = "true") 
    User toEntity(UserCreateDto dto);


    // 2. De Entidad a DTO de Respuesta (GETs)
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "fullName", source = "full_name")
    UserResponseDto toResponseDto(User entity);

    // 3. Actualización parcial (PATCH)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true) // El email queda protegido contra modificaciones en este PATCH
    @Mapping(target = "role.id", source = "roleId")
    @Mapping(target = "full_name", source = "fullName")
    void updateEntityFromDto(UserUpdateDto dto, @MappingTarget User entity);

}
