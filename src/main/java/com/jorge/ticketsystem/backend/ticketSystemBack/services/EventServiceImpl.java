package com.jorge.ticketsystem.backend.ticketSystemBack.services;


import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.TicketCategoryRepository;
import java.util.List;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventUpdateDto;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Event;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.TicketCategory;
import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.EventMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.EventRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

/*No deben pasar los optional a la capa de Controller deben morir aqui */
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final EventRepository repository;
    private final EventMapper eventMapper;



    @Override
    @Transactional
    public void delete(Long id) {
        // Verificamos existencia antes de proceder con el borrado
        if (!repository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: Evento no encontrado con ID: " + id);
        }
        repository.deleteById(id);

    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> findAll() {
        return repository.findAll().stream()
                .map(eventMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto findById(Long id) {
        return repository.findById(id)
                .map(eventMapper::toResponseDto)
                .orElseThrow(()->new EntityNotFoundException("Evento no encontrado con id " + id)); // Llama al repositorio nativo de Spring Data JPA
    }

    @Override
    @Transactional
    public EventResponseDto create(EventCreateDto eventDto) {
        // Event event = new Event();
        // event.setTitle(eventDto.title());
        // event.setArtist(eventDto.artist());

        //Falta comprobar si existe el ticket para este evento
        // TicketCategory= ticketCategoryRepository.findBy

        //Creamos el evento desde el dto de crear
        // ... mapea el resto de campos que tenga tu EventCreateDto ...
        Event event = eventMapper.toEntity(eventDto);
        
        //Guardamos el dto
        repository.save(event);
        return eventMapper.toResponseDto(event);
        
    }

    @Override
    @Transactional
    public EventResponseDto update(Long id, EventUpdateDto dto) {
        return repository.findById(id).map(eventDb -> {
                    // MapStruct actualiza la entidad existente ignorando los valores null del DTO
                    eventMapper.updateEntityFromDto(dto, eventDb);

                    // Guardamos la entidad modificada y la devolvemos envuelta en el Optional
                    repository.save(eventDb);
                    // Devuelve el mismo DTO de actualización envuelto en un Optional (según tu interfaz)
                    return eventMapper.toResponseDto(eventDb);
                })
                .orElseThrow(() -> new EntityNotFoundException("No existe el Evento buscado"));
    }

}
