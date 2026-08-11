package com.jorge.ticketsystem.backend.ticketSystemBack.exception;

// El asiento ya está reservado/vendido por otra Order (comprobación explícita por status)
public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(Long seatId) {
        super("El asiento con ID " + seatId + " ya no está disponible");
    }
}