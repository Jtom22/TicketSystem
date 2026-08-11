package com.jorge.ticketsystem.backend.ticketSystemBack.scheduling;


import java.time.LocalDateTime;
import java.util.List;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
 
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Order;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.OrderStatus;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Seat;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.SeatStatus;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.OrderRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.SeatRepository;
 
import lombok.RequiredArgsConstructor;
 
@Component
@RequiredArgsConstructor
public class SeatReservationExpirationTask {
 
    private static final Logger log = LoggerFactory.getLogger(SeatReservationExpirationTask.class);
 
    private final SeatRepository seatRepository;
    private final OrderRepository orderRepository;
 
    // Cada 60.000 ms (1 minuto). AjustaR este valor según cuánto margen quieras
    // dar entre que una reserva expira y de verdad se libera para otra persona.
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void releaseExpiredReservations() {
        List<Seat> expiredSeats = seatRepository.findExpiredReservedSeats(
                SeatStatus.RESERVADO_TEMPORAL, LocalDateTime.now());
 
        if (expiredSeats.isEmpty()) {
            return; // nada que hacer, evitamos ruido en el log en cada ejecución
        }
 
        for (Seat seat : expiredSeats) {
            Order order = seat.getReservedByOrder();
 
            seat.setStatus(SeatStatus.DISPONIBLE);
            seat.setReservedByOrder(null);
            seatRepository.save(seat);
 
            // Solo tocamos la Order si sigue PENDIENTE— si por lo que sea ya estaba
            // COMPLETADO (alguien pagó justo antes de que corriera este job) o CANCELADO,
            // no queremos pisar ese estado con EXPIRADO por error.
            if (order != null && order.getStatus() == OrderStatus.PENDIENTE) {
                order.setStatus(OrderStatus.EXPIRADO);
                orderRepository.save(order);
            }
        }
 
        log.info("Liberados {} asiento(s) por reserva expirada", expiredSeats.size());
    }
}
 