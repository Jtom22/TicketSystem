package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.TicketCategory;
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
 * lógica de reserva de asientos, el cálculo del total en servidor, y la
 * condición de carrera que @Version está pensado para resolver.
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

    private Seat seatDisponibleConPrecio(Long seatId, int precio) {
        TicketCategory categoria = new TicketCategory();

        categoria.setPrice(BigDecimal.valueOf(precio));

        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setStatus(SeatStatus.DISPONIBLE);
        seat.setTicketCategory(categoria);
        return seat;
    }

    @Test
    void createOrder_cuandoElUsuarioNoExiste_lanzaEntityNotFoundException() {
        OrderCreateDto dto = new OrderCreateDto(999L, List.of(1L));
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createOrder_cuandoElAsientoEstaDisponible_loReservaYFijaEstadoYExpiracionEnServidor() {
        OrderCreateDto dto = new OrderCreateDto(3L, List.of(10L));

        Order orderSinGuardar = new Order();
        Order orderGuardada = new Order();
        orderGuardada.setId(1L);

        Seat seatDisponible = seatDisponibleConPrecio(10L, 30);

        OrderResponseDto responseDto = new OrderResponseDto(
                1L, 30, "PENDIENTE", LocalDateTime.now().plusMinutes(10), 3L);

        when(userRepository.existsById(3L)).thenReturn(true);
        when(orderMapper.toEntity(dto)).thenReturn(orderSinGuardar);
        when(orderRepository.save(any(Order.class))).thenReturn(orderGuardada);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seatDisponible));
        when(orderMapper.toResponseDto(orderGuardada)).thenReturn(responseDto);

        OrderResponseDto result = orderService.createOrder(dto);

        // 1. 🚨 CAPTURAMOS la orden exacta que ha procesado y guardado el servicio
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order ordenGuardadaEnServicio = orderCaptor.getValue();

        // 2. Hacemos las comprobaciones sobre la orden que el servicio procesó:
        assertThat(ordenGuardadaEnServicio.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(ordenGuardadaEnServicio.getExpires_at()).isAfter(LocalDateTime.now());
        assertThat(ordenGuardadaEnServicio.getExpires_at()).isBefore(LocalDateTime.now().plusMinutes(11));

        ArgumentCaptor<Seat> seatCaptor = ArgumentCaptor.forClass(Seat.class);
        verify(seatRepository).saveAndFlush(seatCaptor.capture());
        Seat seatGuardado = seatCaptor.getValue();
        assertThat(seatGuardado.getStatus()).isEqualTo(SeatStatus.RESERVADO_TEMPORAL);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void createOrder_conVariosAsientos_sumaElPrecioDeCadaUno() {
        OrderCreateDto dto = new OrderCreateDto(3L, List.of(10L, 11L));

        Order orderSinGuardar = new Order();
        orderSinGuardar.setId(1L);

        Seat asientoVip = seatDisponibleConPrecio(10L, 50);
        Seat asientoGeneral = seatDisponibleConPrecio(11L, 20);

        when(userRepository.existsById(3L)).thenReturn(true);
        when(orderMapper.toEntity(dto)).thenReturn(orderSinGuardar);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(asientoVip));
        when(seatRepository.findById(11L)).thenReturn(Optional.of(asientoGeneral));

        // // Ejecutas el servicio
        orderService.createOrder(dto);

        // 1. Capturas la orden que el servicio realmente procesó
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order ordenProcesada = orderCaptor.getValue();

        // 2. Compruebas que la suma dio 70 en el objeto real
        assertThat(ordenProcesada.getTotal_amount()).isEqualByComparingTo(BigDecimal.valueOf(70));

    }

    @Test
    void createOrder_cuandoElAsientoYaEstaReservado_lanzaSeatUnavailableException() {
        OrderCreateDto dto = new OrderCreateDto(3L, List.of(10L));

        Order orderGuardada = new Order();
        orderGuardada.setId(1L);

        Seat seatYaReservado = new Seat();
        seatYaReservado.setId(10L);
        seatYaReservado.setStatus(SeatStatus.RESERVADO_TEMPORAL); // ya no está DISPONIBLE

        when(userRepository.existsById(3L)).thenReturn(true);
        when(orderMapper.toEntity(dto)).thenReturn(new Order());
        // when(orderRepository.save(any(Order.class))).thenReturn(orderGuardada);
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
        OrderCreateDto dto = new OrderCreateDto(3L, List.of(10L));

        Order orderGuardada = new Order();
        orderGuardada.setId(1L);

        Seat seatDisponible = seatDisponibleConPrecio(10L, 30);

        when(userRepository.existsById(3L)).thenReturn(true);
        when(orderMapper.toEntity(dto)).thenReturn(new Order());
        // when(orderRepository.save(any(Order.class))).thenReturn(orderGuardada);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seatDisponible));
        when(seatRepository.saveAndFlush(any(Seat.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Seat.class, 10L));

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(SeatConflictException.class);
    }

    @Test
    void createOrder_cuandoElAsientoNoExiste_lanzaEntityNotFoundException() {
        OrderCreateDto dto = new OrderCreateDto(3L, List.of(999L));

        Order orderGuardada = new Order();
        orderGuardada.setId(1L);

        when(userRepository.existsById(3L)).thenReturn(true);
        when(orderMapper.toEntity(dto)).thenReturn(new Order());
        // when(orderRepository.save(any(Order.class))).thenReturn(orderGuardada);
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