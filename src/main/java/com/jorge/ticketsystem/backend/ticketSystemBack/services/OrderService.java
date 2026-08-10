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



public interface OrderService {

    OrderResponseDto createOrder(OrderCreateDto dto);

    OrderResponseDto updateOrder(Long id, OrderUpdateDto dto);

    Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable);

    Page<OrderResponseDto> getAllOrders(Pageable pageable);

    void deleteOrder(Long id);
}