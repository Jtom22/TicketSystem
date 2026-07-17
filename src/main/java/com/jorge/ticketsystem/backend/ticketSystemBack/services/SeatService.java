package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatUpdateDto;

public interface SeatService {

    List<SeatResponseDto> findAll();
    
    //Buscar todos los Asientos de una categoria concreta (Regular,Premium,VIP...)
    Page<SeatResponseDto> getAllByCategory(Long categoryId, Pageable pageable);

    SeatResponseDto findById(Long id);

    SeatResponseDto create(SeatCreateDto createDto, Long idTkCategory);

    SeatResponseDto update(Long id,SeatUpdateDto updateDto);

    void delete(Long id);
}
