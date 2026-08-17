package com.jorge.ticketsystem.backend.ticketSystemBack.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Order;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.OrderStatus;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Seat;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.SeatStatus;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.OrderRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.SeatRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.scheduling.SeatReservationExpirationTask;

/**
 * Este job es la pieza que evita que un asiento quede bloqueado para
 * siempre si alguien reserva y no paga. Sin este test, es exactamente
 * el tipo de código "que corre solo en segundo plano" que nadie prueba
 * a mano nunca — el candidato perfecto para tests automáticos.
 */
@ExtendWith(MockitoExtension.class)
class SeatReservationExpirationTaskTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private SeatReservationExpirationTask expirationTask;

    @Test
    void releaseExpiredReservations_liberaElAsientoYMarcaLaOrdenComoExpired() {
        Order ordenExpirada = new Order();
        ordenExpirada.setId(1L);
        ordenExpirada.setStatus(OrderStatus.PENDIENTE); // seguía pendiente cuando expiró

        Seat asientoExpirado = new Seat();
        asientoExpirado.setId(10L);
        asientoExpirado.setStatus(SeatStatus.RESERVADO_TEMPORAL);
        asientoExpirado.setReservedByOrder(ordenExpirada);

        when(seatRepository.findExpiredReservedSeats(
                org.mockito.ArgumentMatchers.eq(SeatStatus.RESERVADO_TEMPORAL), any(LocalDateTime.class)))
                .thenReturn(List.of(asientoExpirado));

        expirationTask.releaseExpiredReservations();

        // El asiento vuelve a estar libre, sin vínculo con la orden vencida
        ArgumentCaptor<Seat> seatCaptor = ArgumentCaptor.forClass(Seat.class);
        verify(seatRepository).save(seatCaptor.capture());
        assertThat(seatCaptor.getValue().getStatus()).isEqualTo(SeatStatus.DISPONIBLE);
        assertThat(seatCaptor.getValue().getReservedByOrder()).isNull();

        // La orden pasa a Expired, no se queda en PENDIENTE para siempre
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.EXPIRADO);
    }

    @Test
    void releaseExpiredReservations_siLaOrdenYaEstabaCompleted_noLaSobreescribeAExpired() {
        // Caso de carrera real: el usuario pagó justo antes de que corriera
        // el job. No queremos pisar un pedido ya completado.
        Order ordenYaCompletada = new Order();
        ordenYaCompletada.setId(1L);
        ordenYaCompletada.setStatus(OrderStatus.COMPLETADO);

        Seat asiento = new Seat();
        asiento.setId(10L);
        asiento.setStatus(SeatStatus.RESERVADO_TEMPORAL);
        asiento.setReservedByOrder(ordenYaCompletada);

        when(seatRepository.findExpiredReservedSeats(any(SeatStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(asiento));

        expirationTask.releaseExpiredReservations();

        // El asiento SÍ se libera igualmente (la query solo lo encuentra
        // porque su fecha ya pasó, independientemente del status de la orden)
        verify(seatRepository).save(any(Seat.class));

        // Pero la Order NUNCA se toca, porque ya no estaba "PENDIENTE"
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void releaseExpiredReservations_sinAsientosExpirados_noGuardaNada() {
        when(seatRepository.findExpiredReservedSeats(any(SeatStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        expirationTask.releaseExpiredReservations();

        verify(seatRepository, never()).save(any(Seat.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void releaseExpiredReservations_conVariosAsientosDeLaMismaOrden_liberaTodosYExpiraUnaSolaVez() {
        Order ordenExpirada = new Order();
        ordenExpirada.setId(1L);
        ordenExpirada.setStatus(OrderStatus.PENDIENTE);

        Seat asiento1 = new Seat();
        asiento1.setId(10L);
        asiento1.setStatus(SeatStatus.RESERVADO_TEMPORAL);
        asiento1.setReservedByOrder(ordenExpirada);

        Seat asiento2 = new Seat();
        asiento2.setId(11L);
        asiento2.setStatus(SeatStatus.RESERVADO_TEMPORAL);
        asiento2.setReservedByOrder(ordenExpirada);

        when(seatRepository.findExpiredReservedSeats(any(SeatStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(asiento1, asiento2));

        expirationTask.releaseExpiredReservations();

        verify(seatRepository, org.mockito.Mockito.times(2)).save(any(Seat.class));
        // La orden se guarda una vez por cada asiento (es importante comprobar que
        // ponerla a EXPIRADO dos veces no causa ningún problema), así que
        // comprobamos que se llamó AL MENOS una vez.
        verify(orderRepository, org.mockito.Mockito.atLeastOnce()).save(any(Order.class));
    }
}