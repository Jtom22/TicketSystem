package com.jorge.ticketsystem.backend.controllers;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Order;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.OrderStatus;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Seat;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.SeatStatus;
import com.jorge.ticketsystem.backend.ticketSystemBack.exception.SeatConflictException;
import com.jorge.ticketsystem.backend.ticketSystemBack.exception.SeatUnavailableException;
import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.OrderMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.OrderRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.SeatRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.UserRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.OrderServiceImpl;

import jakarta.persistence.EntityNotFoundException;

/**
 * Estos son los tests que de verdad importan del proyecto: prueban la
 * lógica de reserva de asientos, incluida la condición de carrera que
 * @Version está pensado para resolver. Sin ellos, ese fragmento de código
 * es el más "peligroso" de tocar a futuro sin darse cuenta de que se rompió.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_cuandoElUsuarioNoExiste_lanzaEntityNotFoundException() {
        OrderCreateDto dto = new OrderCreateDto(45, 999L, List.of(1L));
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createOrder_cuandoElAsientoEstaDisponible_loReservaYFijaEstadoYExpiracionEnServidor() {
        OrderCreateDto dto = new OrderCreateDto(45, 3L, List.of(10L));

        Order orderSinGuardar = new Order();
        Order orderGuardada = new Order();
        orderGuardada.setId(1L);

        Seat seatDisponible = new Seat();
        seatDisponible.setId(10L);
        seatDisponible.setStatus(SeatStatus.DISPONIBLE);

        OrderResponseDto responseDto = new OrderResponseDto(1L, 45, "Pending", LocalDateTime.now().plusMinutes(10), 3L);

        when(userRepository.existsById(3L)).thenReturn(true);
        when(orderMapper.toEntity(dto)).thenReturn(orderSinGuardar);
        when(orderRepository.save(orderSinGuardar)).thenReturn(orderGuardada);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seatDisponible));
        when(orderMapper.toResponseDto(orderGuardada)).thenReturn(responseDto);

        OrderResponseDto result = orderService.createOrder(dto);

        // El propio servicio debe haber fijado esto, NO el cliente:
        assertThat(orderSinGuardar.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(orderSinGuardar.getExpires_at()).isAfter(LocalDateTime.now());
        assertThat(orderSinGuardar.getExpires_at()).isBefore(LocalDateTime.now().plusMinutes(11));

        // El asiento debe haber quedado bloqueado, vinculado a la orden:
        ArgumentCaptor<Seat> seatCaptor = ArgumentCaptor.forClass(Seat.class);
        org.mockito.Mockito.verify(seatRepository).saveAndFlush(seatCaptor.capture());
        Seat seatGuardado = seatCaptor.getValue();
        assertThat(seatGuardado.getStatus()).isEqualTo(SeatStatus.RESERVADO_TEMPORAL);
        assertThat(seatGuardado.getReservedByOrder()).isEqualTo(orderGuardada);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void createOrder_cuandoElAsientoYaEstaReservado_lanzaSeatUnavailableException() {
        OrderCreateDto dto = new OrderCreateDto(45, 3L, List.of(10L));

        Order orderGuardada = new Order();
        orderGuardada.setId(1L);

        Seat seatYaReservado = new Seat();
        seatYaReservado.setId(10L);
        seatYaReservado.setStatus(SeatStatus.RESERVADO_TEMPORAL); // ya no está DISPONIBLE

        when(userRepository.existsById(3L)).thenReturn(true);
        when(orderMapper.toEntity(dto)).thenReturn(new Order());
        when(orderRepository.save(any(Order.class))).thenReturn(orderGuardada);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seatYaReservado));

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(SeatUnavailableException.class);
    }

    @Test
    void createOrder_cuandoHayChoqueDeVersionEnElGuardado_lanzaSeatConflictException() {
        // Simula el caso real de dos peticiones casi simultáneas sobre el
        // mismo asiento: la comprobación de status pasa para las dos (ambas
        // lo ven DISPONIBLE), pero Hibernate detecta el conflicto en el
        // saveAndFlush() de la segunda, gracias al @Version de Seat.
        OrderCreateDto dto = new OrderCreateDto(45, 3L, List.of(10L));

        Order orderGuardada = new Order();
        orderGuardada.setId(1L);

        Seat seatDisponible = new Seat();
        seatDisponible.setId(10L);
        seatDisponible.setStatus(SeatStatus.DISPONIBLE);

        when(userRepository.existsById(3L)).thenReturn(true);
        when(orderMapper.toEntity(dto)).thenReturn(new Order());
        when(orderRepository.save(any(Order.class))).thenReturn(orderGuardada);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seatDisponible));
        when(seatRepository.saveAndFlush(any(Seat.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Seat.class, 10L));

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(SeatConflictException.class);
    }

    @Test
    void createOrder_cuandoElAsientoNoExiste_lanzaEntityNotFoundException() {
        OrderCreateDto dto = new OrderCreateDto(45, 3L, List.of(999L));

        Order orderGuardada = new Order();
        orderGuardada.setId(1L);

        when(userRepository.existsById(3L)).thenReturn(true);
        when(orderMapper.toEntity(dto)).thenReturn(new Order());
        when(orderRepository.save(any(Order.class))).thenReturn(orderGuardada);
        when(seatRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteOrder_cuandoNoExiste_lanzaEntityNotFoundException() {
        when(orderRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.deleteOrder(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
