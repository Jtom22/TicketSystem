package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.SeatMapperImpl;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.seats.SeatUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Seat;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.SeatRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.TicketCategoryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService{


    private final SeatMapperImpl seatMapper;
    private final SeatRepository seatRepository;
    private final TicketCategoryRepository ticketCategoryRepository; 

    @Override
    public SeatResponseDto create(SeatCreateDto createDto, Long idTkCategory) {
        
        if (!seatRepository.existsById(idTkCategory)) {
            throw new EntityNotFoundException("No existe el tipo de categoria de id "+idTkCategory);
        }

        Seat seat= seatMapper.toEntity(createDto);//Transformamos nuestro dto en entidad
        seat.setTicketCategory(ticketCategoryRepository.getReferenceById(idTkCategory));//Asociamos la relación mediante una referencia Proxy eficiente->el proxy:ticketCategoryRepository.getReferenceById(idTkCategory)

        //Aqui guardamos el asiento el asiento en la bbdd
        // Seat savedSeat= seatRepository.save(seat); lo incluimos directamente abajo

        return seatMapper.toResponseDto(seatRepository.save(seat)); //Retornamos el DTO de salida mapeado con su ID y @Version asignados

      

    }

    @Override
    public void delete(Long id) {
        
        seatRepository.findById(id)
        .orElseThrow(()->new EntityNotFoundException("No existe entidad con id "+ id));

        seatRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    @Override
    public List<SeatResponseDto> findAll() {
        
        return seatRepository.findAll().stream()//Con stream indicamos que separamos la lista
        .map(seat->seatMapper.toResponseDto(seat))//Cada uno de ellos lo convertimos al  formato buscado
        .toList();//Java 16+: crea una lista inmutable de forma más eficiente

        //Otra opcion
        // return seatRepository.findAll().stream()
        // .map(seatMapper::toResponseDto)
        // toList();
    }

    @Override
    public Page<SeatResponseDto> getAllByCategory(Long categoryId, Pageable pageable) {
        return seatRepository.findByTicketCategoryId(categoryId, pageable)
                .map(seatMapper::toResponseDto);
    }


    @Override
    public SeatResponseDto findById(Long id) {
             //Opcion1
        // Seat seat= seatRepository.findById(id)
        // .orElseThrow(()->new EntityNotFoundException("Entidad no encontrada"));
        // return seatMapper.toResponseDto(seat);
        
        //Opcion2
         return seatRepository.findById(id)
            .map(seatMapper::toResponseDto)
            .orElseThrow(() -> new EntityNotFoundException("Asiento no encontrado con ID: " + id));



    }


    @Override
    public SeatResponseDto update(Long id, SeatUpdateDto updateDto) {
        return seatRepository.findById(id)
        .map(seat-> {
            seatMapper.updateEntityFromDto(updateDto, seat);
            seatRepository.save(seat);
            return seatMapper.toResponseDto(seat);
        })
        .orElseThrow(()->new EntityNotFoundException("Asiento no encontrado con ID: " + id));
        
        
    }

    
}
