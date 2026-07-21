package com.jorge.ticketsystem.backend.ticketSystemBack.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.IssuedTicketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/issued-tickets")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class IssuedTicketController {

    private final IssuedTicketService issuedTicketService;

    // 1. CREAR (POST) -> /api/v1/issued-tickets
    @PostMapping
    public ResponseEntity<IssuedTicketResponseDto> crearTicket(@Valid @RequestBody IssuedTicketCreateDto dto) {
        IssuedTicketResponseDto nuevoTicket = issuedTicketService.create(dto);
        return new ResponseEntity<>(nuevoTicket, HttpStatus.CREATED);
    }

    // 2. BUSCAR POR ID (GET) -> /api/v1/issued-tickets/5
    @GetMapping("/{id}")
    public ResponseEntity<IssuedTicketResponseDto> obtenerTicketPorId(@PathVariable Long id) {
        IssuedTicketResponseDto ticket = issuedTicketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    // 3. OBTENER TODOS PAGINADOS (GET) -> /api/v1/issued-tickets?page=0&size=10&sort=id,desc
    @GetMapping
    public ResponseEntity<Page<IssuedTicketResponseDto>> obtenerTodosLosTickets(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<IssuedTicketResponseDto> tickets = issuedTicketService.getAllTickets(pageable);
        return ResponseEntity.ok(tickets);
    }

    // 4. TICKETS POR ORDEN PAGINADOS (GET) -> /api/v1/issued-tickets/order/12
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Page<IssuedTicketResponseDto>> obtenerTicketsPorOrden(
            @PathVariable Long orderId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<IssuedTicketResponseDto> tickets = issuedTicketService.getTicketByOrder(orderId, pageable);
        return ResponseEntity.ok(tickets);
    }

    // 5. TICKETS POR ASIENTO PAGINADOS (GET) -> /api/v1/issued-tickets/seat/45
    @GetMapping("/seat/{seatId}")
    public ResponseEntity<Page<IssuedTicketResponseDto>> obtenerTicketsPorAsiento(
            @PathVariable Long seatId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<IssuedTicketResponseDto> tickets = issuedTicketService.getTicketBySeat(seatId, pageable);
        return ResponseEntity.ok(tickets);
    }

    // 6. ACTUALIZAR PARCIAL (PATCH) -> /api/v1/issued-tickets/5
    @PatchMapping("/{id}")
    public ResponseEntity<IssuedTicketResponseDto> actualizarTicket(
            @PathVariable Long id, 
            @Valid @RequestBody IssuedTicketUpdateDto dto) {
        IssuedTicketResponseDto actualizado = issuedTicketService.update(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    // 7. ELIMINAR (DELETE) -> /api/v1/issued-tickets/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long id) {
        issuedTicketService.delete(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content
    }
}
