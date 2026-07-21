package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import java.util.List;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventUpdateDto;


public interface EventService {
    List<EventResponseDto> findAll();

    EventResponseDto findById(Long id);

    EventResponseDto create(EventCreateDto event);

    EventResponseDto update(Long id, EventUpdateDto dto);

    void delete(Long id);

}
