package com.jorge.ticketsystem.backend.ticketSystemBack.dto.error;

import java.time.LocalDateTime;
import java.util.List;
 
public record ApiErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<String> details // Solo se rellena en errores de validación (@Valid)
) {
    public ApiErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }
 
    public ApiErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(LocalDateTime.now(), status, error, message, path, details);
    }
}