package com.jorge.ticketsystem.backend.ticketSystemBack.controllers;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.SeatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/seats")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // Crear un asiento dentro de una categoría
    @PostMapping("/ticket-categories/{categoryId}/seats")
    public ResponseEntity<SeatResponseDto> create(
            @PathVariable Long categoryId,
            @Valid @RequestBody SeatCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatService.create(dto, categoryId));
    }

   
    // Listar todos los asientos de una categoría específica, paginado
    // Ejemplo: GET /seats/ticket-categories/5/seats?page=0&size=50&sort=seatNumber,asc
    @GetMapping("/ticket-categories/{categoryId}/seats")
    public ResponseEntity<Page<SeatResponseDto>> getAllByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(seatService.getAllByCategory(categoryId, pageable));
    }

    // Obtener un asiento específico por su propio ID
    @GetMapping("/{id}")
    public ResponseEntity<SeatResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.findById(id));
    }

    // Actualizar un asiento por su ID
    @PatchMapping("/{id}")
    public ResponseEntity<SeatResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody SeatUpdateDto dto) {
        return ResponseEntity.ok(seatService.update(id, dto));
    }

    // Eliminar un asiento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        seatService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
