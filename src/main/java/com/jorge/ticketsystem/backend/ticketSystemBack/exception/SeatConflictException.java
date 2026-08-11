package com.jorge.ticketsystem.backend.ticketSystemBack.exception;

// Dos peticiones llegaron literalmente al mismo tiempo y chocaron en el @Version
// (esto lo lanza Hibernate como ObjectOptimisticLockingFailureException;
// la envolvemos para no filtrar una excepción técnica de Hibernate a la API)
public class SeatConflictException extends RuntimeException {
    public SeatConflictException(Long seatId) {
        super("El asiento con ID " + seatId + " ha sido reservado por otra persona en este instante. Inténtalo de nuevo.");
    }
}