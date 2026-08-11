package com.jorge.ticketsystem.backend.ticketSystemBack.services;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Order;
import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.OrderMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.OrderRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.SeatRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.UserRepository;


@Service
@RequiredArgsConstructor // Genera el constructor para la inyección de dependencias (Reemplaza a @Autowired)
public class OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    /**
     * 1. CREAR UNA ORDEN (POST)
     * Gracias al mapper, nos ahorramos buscar el User en la BD.
     */
    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto) {

        if (!orderRepository.existsById(dto.userId())) {
            throw new EntityNotFoundException("No existe el tipo de categoria de id "+dto.userId());
        }
        // MapStruct genera la entidad Order y le asocia un objeto User con su ID asignado y nos ahorramos codigo asi
        Order order = orderMapper.toEntity(dto);
        
        // Guardamos directamente. Si el userId no existe, saltará la restricción de FK de la BD
        Order savedOrder = orderRepository.save(order);
        
        return orderMapper.toResponseDto(savedOrder);
    }

    /**
     * 2. ACTUALIZACIÓN PARCIAL (PATCH)
     * Modifica solo los campos enviados en el record, ignorando los nulos.
     */
    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderUpdateDto dto) {
        // Buscamos la orden existente en la base de datos
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la orden con ID: " + id));

        // MapStruct vuelca los cambios no nulos del DTO sobre la entidad persistida
        orderMapper.updateEntityFromDto(dto, existingOrder);

        // Al estar anotado con @Transactional y ser una entidad gestionada por JPA,
        // los cambios se guardan automáticamente al terminar el método (dirty checking),
        // pero usar .save() es una buena práctica explícita.
        // Order updatedOrder = orderRepository.save(existingOrder);

        // return orderMapper.toResponseDto(updatedOrder);
        return orderMapper.toResponseDto(orderRepository.save(existingOrder));
    }

     /**
     * 3. OBTENER TODAS LAS ÓRDENES DE UN USUARIO (GET)
     * Corregido: Filtra en la base de datos por el ID del usuario recibido.
     */
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable) {
        
        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::toResponseDto);
                
    }
    /**
     * 4. LISTAR TODAS LAS ÓRDENES (GET)
     */
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponseDto);
                 // .toList() nativo de Java 16+ para usar con records
                
    }

    /**
     * 5. ELIMINAR ORDEN (DELETE)
     */
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("No se puede eliminar. No existe la orden con ID: " + id);
        }
        orderRepository.deleteById(id);
    }
}