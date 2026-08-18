package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderUpdateDto;
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

@Service
@RequiredArgsConstructor // Genera el constructor para la inyección de dependencias (Reemplaza a
                         // @Autowired)
public class OrderServiceImpl implements OrderService {

    // Minutos que dura una reserva antes de expirar. Fijo aquí, no lo decide
    // el cliente — así nadie puede mandar una fecha de expiración a su gusto.
    private static final long MINUTOS_RESERVA = 10;

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    // 1. CREAR UNA ORDEN (POST) + RESERVA TEMPORAL DE ASIENTOS

    // Aquí es donde bloqueamos los asientos mientras dura la compra:
    // 1) Comprobamos que el usuario existe.
    // 2) Guardamos la Order primero (para tener su ID y poder enlazarla a los
    // Seats).
    // 3) Por cada seatId: lo recargamos DENTRO de esta misma transacción,
    // comprobamos
    // que sigue DISPONIBLE, y lo pasamos a RESERVADO_TEMPORAL guardándolo — es ese
    // guardado el que activa de verdad el @Version (a diferencia de antes, que solo
    // se leía el asiento y nunca se tocaba).
    // 4) Si dos peticiones chocan sobre el mismo asiento, Hibernate lanza
    // ObjectOptimisticLockingFailureException al hacer flush/save — la capturamos
    // y la traducimos a nuestra SeatConflictException.

    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto) {

        if (!userRepository.existsById(dto.userId())) {
            throw new EntityNotFoundException("No existe el usuario con id " + dto.userId());
        }

        Order order = orderMapper.toEntity(dto);
        // El estado inicial y la duración de la reserva los decide el servidor,
        // nunca el cliente (ver el porqué en OrderCreateDto).
        order.setStatus(OrderStatus.PENDIENTE);
        order.setExpires_at(LocalDateTime.now().plusMinutes(MINUTOS_RESERVA));

        //para tener un order que su id no se a nulo
        Order savedOrder = orderRepository.save(order);
        // El total lo calcula el servidor, sumando el precio REAL de la
        // categoría de cada asiento.
        BigDecimal total = BigDecimal.ZERO;
        for (Long seatId : dto.seatIds()) {
            // Le pasamos 'order' (la que se está creando) para asociar el asiento
            total = total.add(reserveSeat(seatId, order));//reserve seat devuelve BigDecimal
        }

        order.setTotal_amount(total);
        orderRepository.save(savedOrder);   // segundo save, para guardar el total amount 

        return orderMapper.toResponseDto(savedOrder);
    }

    private BigDecimal reserveSeat(Long seatId, Order order) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("No existe el asiento con id " + seatId));

        // Comprobación explícita: el caso normal de "otra Order ya lo tenía"
        if (seat.getStatus() != SeatStatus.DISPONIBLE) {
            throw new SeatUnavailableException(seatId);
        }

        seat.setStatus(SeatStatus.RESERVADO_TEMPORAL);
        seat.setReservedByOrder(order);

        try {
            // saveAndFlush() (no save()) es importante aqui se fuerza el UPDATE
            // inmediato en bbdd , para que si hay conflicto de @Version salte
            // en ESTE punto del bucle (y sepamos que asiento ha sido exactamente),
            // en vez de acumularse y saltar todo junto al final de la transacción.
            seatRepository.saveAndFlush(seat);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new SeatConflictException(seatId);
        }
        return seat.getTicketCategory().getPrice();
    }

    // 2. ACTUALIZACIÓN PARCIAL (PATCH)
    // Modifica solo los campos enviados en el record, ignorando los nulos.

    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderUpdateDto dto) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la orden con ID: " + id));

        orderMapper.updateEntityFromDto(dto, existingOrder);

        return orderMapper.toResponseDto(orderRepository.save(existingOrder));
    }

    // 3. OBTENER TODAS LAS ÓRDENES DE UN USUARIO (GET)

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable) {

        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::toResponseDto);

    }

    // 4. LISTAR TODAS LAS ÓRDENES (GET)

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponseDto);
    }

    // 5. ELIMINAR ORDEN (DELETE)

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("No se puede eliminar. No existe la orden con ID: " + id);
        }
        orderRepository.deleteById(id);
    }
}
