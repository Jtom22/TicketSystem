package com.jorge.ticketsystem.backend.ticketSystemBack.controllers;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderUpdateDto;

import com.jorge.ticketsystem.backend.ticketSystemBack.services.OrderServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;




    /**
     * 1. CREAR UNA ORDEN (POST)
     * URL: POST http://localhost:8080/api/v1/orders
     */
    @PostMapping("/api/v1/orders")
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderCreateDto dto) {
        OrderResponseDto response = orderService.createOrder(dto);
        // Devolvemos un estado 201 Created junto al DTO generado
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2. ACTUALIZACIÓN PARCIAL DE UNA ORDEN (PATCH)
     * URL: PATCH http://localhost:8080/api/v1/orders/1
     */
    @PatchMapping("/api/v1/orders/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long id, 
            @Valid @RequestBody OrderUpdateDto dto) {
        OrderResponseDto response = orderService.updateOrder(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
      /**
     * 3. OBTENER ÓRDENES DE UN USUARIO PAGINADAS
     * Cambiado: Recibe Pageable y devuelve Page<OrderResponseDto>
     */
    @GetMapping("/api/v1/users/{userId}/orders")
    public ResponseEntity<Page<OrderResponseDto>> getOrdersByUserId(Long userId, Pageable pageable) {
       
        
        // El objeto Page de Spring tiene su propio método .map() nativo
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, pageable));
         
    }

    /**
     * 4. ELIMINAR UNA ORDEN (DELETE)
     * URL: DELETE http://localhost:8080/api/v1/orders/1
     */
    @DeleteMapping("/api/v1/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        // Devolvemos 24 No Content ya que la eliminación fue exitosa y no hay cuerpo de respuesta
        return ResponseEntity.noContent().build();
    }
}