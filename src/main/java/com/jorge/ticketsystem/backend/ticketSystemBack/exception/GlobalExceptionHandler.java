package com.jorge.ticketsystem.backend.ticketSystemBack.exception;

import java.util.List;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.context.request.WebRequest;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.error.ApiErrorResponse;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;


//
@Hidden
@RestControllerAdvice // 👈 Le dice a Spring: "Vigila todos los controladores de la app"
public class GlobalExceptionHandler {

    // 404 — el recurso que se busca no existe.
    // Es la que más usas: TicketCategory, Seat, Order, IssuedTicket, User...
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }
 
    // 409 — el recurso ya existe (ej: email duplicado en el registro)
    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityExists(
            EntityExistsException ex, WebRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }
 
    // 400 — @Valid ha fallado en algún DTO de entrada (EventCreateDto, RegisterRequestDto...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {
 
                //Convertimos los mensajes de error en uno legible
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
 
        //generamos el cuerpo del error con el formato del dto
        //Usamos ResponseEntity porque usamos el body para enviar un texto mas detallado
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Error de validación en los datos enviados",
                extractPath(request),
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
 
    // 401 — login con email/contraseña incorrectos
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos", request);
    }
 
    // 401 — cualquier otro fallo de autenticación (token corrupto, usuario deshabilitado...)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException ex, WebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "No autenticado: " + ex.getMessage(), request);
    }
 
    // 403 — el usuario está autenticado, pero no tiene el rol necesario (hasRole("ADMIN"))
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción", request);
    }

    
    
 
    // 500 — red de seguridad para cualquier cosa no contemplada arriba.
    // Sin esto, un error inesperado devolvería el HTML de error por defecto de Spring.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error inesperado", request);
    }


    @ExceptionHandler(SeatUnavailableException.class)
public ResponseEntity<ApiErrorResponse> handleSeatUnavailable(SeatUnavailableException ex, WebRequest request) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
}

@ExceptionHandler(SeatConflictException.class)
public ResponseEntity<ApiErrorResponse> handleSeatConflict(SeatConflictException ex, WebRequest request) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
}

@ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
public ResponseEntity<ApiErrorResponse> handleOptimisticLock(
        org.springframework.orm.ObjectOptimisticLockingFailureException ex, WebRequest request) {
    return buildResponse(HttpStatus.CONFLICT,
        "El recurso fue modificado por otra petición al mismo tiempo. Inténtalo de nuevo.", request);
}

    
 
    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status, String message, WebRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                extractPath(request)
        );
        return ResponseEntity.status(status).body(body);
    }
 
    private String extractPath(WebRequest request) {
        // WebRequest.getDescription(false) devuelve algo como "uri=/api/v1/events/5"
        return request.getDescription(false).replace("uri=", "");
    }
}
