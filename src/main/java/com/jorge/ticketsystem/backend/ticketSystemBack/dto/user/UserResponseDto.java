package com.jorge.ticketsystem.backend.ticketSystemBack.dto.user;

import java.util.List;

public record UserResponseDto(

    Long id,
    String fullName,
    String email,
    boolean enabled,
    List<String> roles
) {

}
