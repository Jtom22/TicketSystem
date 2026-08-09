package com.jorge.ticketsystem.backend.ticketSystemBack.dto.auth;

import java.util.List;

public record AuthResponseDto(
    String token,
    String email,
    String fullName,
    List<String> roles
) {}
