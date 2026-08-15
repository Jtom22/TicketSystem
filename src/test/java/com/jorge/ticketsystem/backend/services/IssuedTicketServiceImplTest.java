package com.jorge.ticketsystem.backend.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
 
import java.time.LocalDateTime;
import java.util.Optional;
 
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.IssuedTicket;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Order;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Seat;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.SeatStatus;
import com.jorge.ticketsystem.backend.ticketSystemBack.exception.SeatUnavailableException;
import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.IssuedTicketMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.IssuedTicketRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.OrderRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.SeatRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.IssuedTicketServiceImpl;

import jakarta.persistence.EntityNotFoundException;
 
/**
 * IssuedTicketServiceImpl.create() es el punto donde se confirma la venta
 * de verdad — tiene 4 comprobaciones de negocio en cadena antes de emitir
 * el ticket. Cada una necesita su propio test, porque cada una protege
 * contra un fallo distinto (ver comentarios en el propio método).
 */
@ExtendWith(MockitoExtension.class)
class IssuedTicketServiceImplTest {
 
    @Mock
    private IssuedTicketRepository issuedTicketRepository;
 
    @Mock
    private IssuedTicketMapper issuedTicketMapper;
 
    @Mock
    private OrderRepository orderRepository;
 
    @Mock
    private SeatRepository seatRepository;
 
    @InjectMocks
    private IssuedTicketServiceImpl issuedTicketService;
 
    private Order ordenValida() {
        Order order = new Order();
        order.setId(1L);
        order.setExpires_at(LocalDateTime.now().plusMinutes(5)); // todavía no expiró
        return order;
    }
 
    @Test
    void create_cuandoLaOrdenNoExiste_lanzaEntityNotFoundException() {
        IssuedTicketCreateDto dto = new IssuedTicketCreateDto("qr-123", 999L, 10L);
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
 
        assertThatThrownBy(() -> issuedTicketService.create(dto))
                .isInstanceOf(EntityNotFoundException.class);
    }
 
    @Test
    void create_cuandoElAsientoNoExiste_lanzaEntityNotFoundException() {
        IssuedTicketCreateDto dto = new IssuedTicketCreateDto("qr-123", 1L, 999L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(ordenValida()));
        when(seatRepository.findById(999L)).thenReturn(Optional.empty());
 
        assertThatThrownBy(() -> issuedTicketService.create(dto))
                .isInstanceOf(EntityNotFoundException.class);
    }
 
    @Test
    void create_cuandoElAsientoNoEstaReservado_lanzaSeatUnavailableException() {
        // Un asiento DISPONIBLE nunca pasó por el paso de reserva — no se
        // puede emitir un ticket directamente sobre él.
        IssuedTicketCreateDto dto = new IssuedTicketCreateDto("qr-123", 1L, 10L);
        Order orden = ordenValida();
 
        Seat asientoDisponible = new Seat();
        asientoDisponible.setId(10L);
        asientoDisponible.setStatus(SeatStatus.DISPONIBLE);
 
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(seatRepository.findById(10L)).thenReturn(Optional.of(asientoDisponible));
 
        assertThatThrownBy(() -> issuedTicketService.create(dto))
                .isInstanceOf(SeatUnavailableException.class);
    }
 
    @Test
    void create_cuandoLaReservaYaExpiroPeroElStatusAunNoSeActualizo_lanzaSeatUnavailableException() {
        // Este es el caso que cierra la ventana de hasta 60s: el status en BD
        // todavía dice RESERVADO_TEMPORAL (el @Scheduled no ha pasado todavía),
        // pero la fecha real ya venció. No debe dejar colarse la compra.
        IssuedTicketCreateDto dto = new IssuedTicketCreateDto("qr-123", 1L, 10L);
        Order ordenExpirada = new Order();
        ordenExpirada.setId(1L);
        ordenExpirada.setExpires_at(LocalDateTime.now().minusSeconds(30)); // ya venció
 
        Seat asiento = new Seat();
        asiento.setId(10L);
        asiento.setStatus(SeatStatus.RESERVADO_TEMPORAL);
        asiento.setReservedByOrder(ordenExpirada);
 
        when(orderRepository.findById(1L)).thenReturn(Optional.of(ordenExpirada));
        when(seatRepository.findById(10L)).thenReturn(Optional.of(asiento));
 
        assertThatThrownBy(() -> issuedTicketService.create(dto))
                .isInstanceOf(SeatUnavailableException.class);
    }
 
    @Test
    void create_cuandoElAsientoEstaReservadoPorOtraOrden_lanzaSeatUnavailableException() {
        // El asiento SÍ está RESERVADO_TEMPORAL y sin expirar, pero por la
        // Order de OTRA persona — no se puede emitir el ticket sobre él.
        IssuedTicketCreateDto dto = new IssuedTicketCreateDto("qr-123", 1L, 10L);
        Order miOrden = ordenValida();
 
        Order ordenDeOtroUsuario = new Order();
        ordenDeOtroUsuario.setId(999L);
        ordenDeOtroUsuario.setExpires_at(LocalDateTime.now().plusMinutes(5));
 
        Seat asiento = new Seat();
        asiento.setId(10L);
        asiento.setStatus(SeatStatus.RESERVADO_TEMPORAL);
        asiento.setReservedByOrder(ordenDeOtroUsuario); // ¡no es "miOrden"!
 
        when(orderRepository.findById(1L)).thenReturn(Optional.of(miOrden));
        when(seatRepository.findById(10L)).thenReturn(Optional.of(asiento));
 
        assertThatThrownBy(() -> issuedTicketService.create(dto))
                .isInstanceOf(SeatUnavailableException.class);
    }
 
    @Test
    void create_cuandoTodoEsCorrecto_emiteElTicketYMarcaElAsientoOcupado() {
        IssuedTicketCreateDto dto = new IssuedTicketCreateDto("qr-123", 1L, 10L);
        Order orden = ordenValida();
 
        Seat asiento = new Seat();
        asiento.setId(10L);
        asiento.setStatus(SeatStatus.RESERVADO_TEMPORAL);
        asiento.setReservedByOrder(orden); // reservado por LA MISMA orden
 
        IssuedTicket ticketSinGuardar = new IssuedTicket();
        IssuedTicket ticketGuardado = new IssuedTicket();
        ticketGuardado.setId(1L);
        IssuedTicketResponseDto responseDto = new IssuedTicketResponseDto(1L, "qr-123", 1L, 10L);
 
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(seatRepository.findById(10L)).thenReturn(Optional.of(asiento));
        when(issuedTicketMapper.toEntity(dto)).thenReturn(ticketSinGuardar);
        when(issuedTicketRepository.save(ticketSinGuardar)).thenReturn(ticketGuardado);
        when(issuedTicketMapper.toResponseDto(ticketGuardado)).thenReturn(responseDto);
 
        IssuedTicketResponseDto result = issuedTicketService.create(dto);
 
        // El asiento debe quedar confirmado como vendido, y sin el vínculo
        // temporal (ya no hace falta, el vínculo permanente es el propio ticket)
        ArgumentCaptor<Seat> seatCaptor = ArgumentCaptor.forClass(Seat.class);
        verify(seatRepository).save(seatCaptor.capture());
        assertThat(seatCaptor.getValue().getStatus()).isEqualTo(SeatStatus.OCUPADO);
        assertThat(seatCaptor.getValue().getReservedByOrder()).isNull();
 
        assertThat(result.id()).isEqualTo(1L);
    }
 
    @Test
    void getTicketByOrder_cuandoLaOrdenNoExiste_lanzaEntityNotFoundException() {
        // Cubre el bug que arreglamos: la condición estaba invertida y
        // lanzaba el error justo cuando la orden SÍ existía.
        when(orderRepository.existsById(999L)).thenReturn(false);
 
        assertThatThrownBy(() -> issuedTicketService.getTicketByOrder(999L, null))
                .isInstanceOf(EntityNotFoundException.class);
    }
 
    @Test
    void getTicketByOrder_cuandoLaOrdenExiste_noLanzaExcepcion() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        when(issuedTicketRepository.findByOrderId(any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
 
        // No debe lanzar nada — antes del fix, esto SÍ lanzaba por error
        // (la condición estaba invertida).
        issuedTicketService.getTicketByOrder(1L, org.springframework.data.domain.Pageable.unpaged());
 
        verify(issuedTicketRepository).findByOrderId(any(), any());
    }
}