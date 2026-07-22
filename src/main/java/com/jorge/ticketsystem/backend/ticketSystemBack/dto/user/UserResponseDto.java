package com.jorge.ticketsystem.backend.ticketSystemBack.dto.user;



public record UserResponseDto(

    String fullName,
    String email,
    String password,
    boolean enabled,
    String roleId
) {

}
