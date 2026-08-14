package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventUpdateDto;


public interface EventService {
    Page<EventResponseDto> findAll(Pageable pageable);

    EventResponseDto findById(Long id);

    EventResponseDto create(EventCreateDto event);

    EventResponseDto update(Long id, EventUpdateDto dto);

    void delete(Long id);

}
