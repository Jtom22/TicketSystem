package com.jorge.ticketsystem.backend.ticketSystemBack.dto.user;

import java.util.List;

public record UserResponseDto(

    Long Id,
    String fullName,
    String email,
    boolean enabled,
    List<String> roles
) {

}
