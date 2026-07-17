package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import java.util.List;


import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryUpdateDto;


public interface TicketCategoryService {
    
    List<TicketCategoryResponseDto> findAll();

    List<TicketCategoryResponseDto>  getAllByEvent(Long eventId);
    
    TicketCategoryResponseDto findById(Long id);
    
    // Recibe DTO de creación -> Devuelve DTO de respuesta
    TicketCategoryResponseDto save(Long eventId,TicketCategoryCreateDto createDto);
    
    // Recibe ID de la URL + DTO de actualización -> Devuelve DTO de respuesta
    TicketCategoryResponseDto update(Long id, TicketCategoryUpdateDto updateDto);
    
    void delete(Long id);
}
