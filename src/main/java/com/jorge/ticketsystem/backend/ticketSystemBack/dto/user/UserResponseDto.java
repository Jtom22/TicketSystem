package com.jorge.ticketsystem.backend.ticketSystemBack.dto.user;



public record UserResponseDto(

     String email,
    String password,
    boolean enabled
) {

}
