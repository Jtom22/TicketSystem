package com.jorge.ticketsystem.backend.ticketSystemBack.controllers;

import java.util.List;

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

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.TicketCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ticketCategory")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class TicketCategoryController {

    private final TicketCategoryService categoryService;

    // Crear categoría dentro de un evento
    @PostMapping("/events/{eventId}/ticket-categories")
    public ResponseEntity<TicketCategoryResponseDto> create(
            @PathVariable Long eventId, 
            @Valid @RequestBody TicketCategoryCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(eventId, dto));
    }

    // Listar todas las categorías de un evento específico
    @GetMapping("/events/{eventId}/ticket-categories")
    public ResponseEntity<List<TicketCategoryResponseDto>> getAllByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(categoryService.getAllByEvent(eventId));
    }

    // Obtener una categoría específica por su propio ID
    @GetMapping("/ticket-categories/{id}")
    public ResponseEntity<TicketCategoryResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    // Actualizar una categoría por su ID
    @PatchMapping("/ticket-categories/{id}")
    public ResponseEntity<TicketCategoryResponseDto> update(
            @PathVariable Long id, 
            @Valid @RequestBody TicketCategoryUpdateDto dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    // Eliminar una categoría
    @DeleteMapping("/ticket-categories/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
