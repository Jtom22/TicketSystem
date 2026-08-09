package com.jorge.ticketsystem.backend.ticketSystemBack.mappers;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Role;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.User;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    // 1. De DTO de Creación a Entidad (POST)


    @Mapping(target = "roles", ignore = true) // O deja que MapStruct mapee List<Role> -> List<String> / List<RoleDto>
    @Mapping(target = "full_name", source = "fullName")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "password", ignore = true) 
    User toEntity(UserCreateDto dto);


    // 2. De Entidad a DTO de Respuesta (GETs)
    @Mapping(target = "roles", ignore = true) // O deja que MapStruct mapee List<Role> -> List<String> / List<RoleDto>
    @Mapping(target = "fullName", source = "full_name")
    UserResponseDto toResponseDto(User entity);

    // 3. Actualización parcial (PATCH)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true) // El email queda protegido contra modificaciones en este PATCH
// Si estás pasando de User (entidad) a UserDto
    @Mapping(target = "roles", ignore = true) // O deja que MapStruct mapee List<Role> -> List<String> / List<RoleDto>
    @Mapping(target = "full_name", source = "fullName")
    void updateEntityFromDto(UserUpdateDto dto, @MappingTarget User entity);



    default List<String> mapRolesToStrings(List<Role> roles) {
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }
    
}
