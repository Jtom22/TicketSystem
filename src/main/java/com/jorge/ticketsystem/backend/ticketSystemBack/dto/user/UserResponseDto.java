package com.jorge.ticketsystem.backend.ticketSystemBack.dto.user;

import java.util.List;

public record UserResponseDto(

    String fullName,
    String email,
    String password,
    boolean enabled,
    List<String> roles
) {

}
