package com.jorge.ticketsystem.backend.ticketSystemBack.services;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Order;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Seat;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.SeatStatus;
import com.jorge.ticketsystem.backend.ticketSystemBack.exception.SeatConflictException;
import com.jorge.ticketsystem.backend.ticketSystemBack.exception.SeatUnavailableException;
import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.OrderMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.OrderRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.SeatRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.UserRepository;


@Service
@RequiredArgsConstructor // Genera el constructor para la inyección de dependencias (Reemplaza a @Autowired)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
 
    
    //   1. CREAR UNA ORDEN (POST) + RESERVA TEMPORAL DE ASIENTOS
     
    //   Aquí es donde bloqueamos los asientos mientras dura la compra:
    //   1) Comprobamos que el usuario existe.
    //   2) Guardamos la Order primero (para tener su ID y poder enlazarla a los Seats).
    //   3) Por cada seatId: lo recargamos DENTRO de esta misma transacción, comprobamos
    //      que sigue DISPONIBLE, y lo pasamos a RESERVADO_TEMPORAL guardándolo — es ese
    //      guardado el que activa de verdad el @Version (a diferencia de antes, que solo
    //      se leía el asiento y nunca se tocaba).
    //   4) Si dos peticiones chocan sobre el mismo asiento, Hibernate lanza
    //      ObjectOptimisticLockingFailureException al hacer flush/save — la capturamos
    //      y la traducimos a nuestra SeatConflictException.
     
    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto) {
 
        if (!userRepository.existsById(dto.userId())) {
            throw new EntityNotFoundException("No existe el usuario con id " + dto.userId());
        }
 
        Order order = orderMapper.toEntity(dto);
        Order savedOrder = orderRepository.save(order);
 
        for (Long seatId : dto.seatIds()) {
            reserveSeat(seatId, savedOrder);
        }
 
        return orderMapper.toResponseDto(savedOrder);
    }
 
    private void reserveSeat(Long seatId, Order order) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("No existe el asiento con id " + seatId));
 
        // Comprobación explícita: el caso normal de "otra Order ya lo tenía"
        if (seat.getStatus() != SeatStatus.DISPONIBLE) {
            throw new SeatUnavailableException(seatId);
        }
 
        seat.setStatus(SeatStatus.RESERVADO_TEMPORAL);
        seat.setReservedByOrder(order);
 
        try {
            // saveAndFlush() (no save()) es importante aquí: fuerza el UPDATE
            // inmediato en BD, para que si hay conflicto de @Version salte
            // en ESTE punto del bucle (y sepamos qué asiento fue exactamente),
            // en vez de acumularse y saltar todo junto al final de la transacción.
            seatRepository.saveAndFlush(seat);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new SeatConflictException(seatId);
        }
    }
 
    
    //  2. ACTUALIZACIÓN PARCIAL (PATCH)
    //  Modifica solo los campos enviados en el record, ignorando los nulos.
    
    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderUpdateDto dto) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la orden con ID: " + id));
 
        orderMapper.updateEntityFromDto(dto, existingOrder);
 
        return orderMapper.toResponseDto(orderRepository.save(existingOrder));
    }
 
     
     
    //  3. OBTENER TODAS LAS ÓRDENES DE UN USUARIO (GET)
    
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable) {
        
        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::toResponseDto);
                
    }
    
    //  4. LISTAR TODAS LAS ÓRDENES (GET)
    
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponseDto);
    }
 
    
    //  5. ELIMINAR ORDEN (DELETE)
    
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("No se puede eliminar. No existe la orden con ID: " + id);
        }
        orderRepository.deleteById(id);
    }
}
 