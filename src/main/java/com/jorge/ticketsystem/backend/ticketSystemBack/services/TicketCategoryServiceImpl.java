package com.jorge.ticketsystem.backend.ticketSystemBack.services;


import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.ticketCategory.TicketCategoryUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.EventRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.TicketCategory;
import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.TicketCategoryMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.TicketCategoryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketCategoryServiceImpl implements TicketCategoryService {


    private final EventRepository eventRepository;
    private final TicketCategoryRepository categoryRepository;
    private final TicketCategoryMapper categoryMapper;



    @Override
    @Transactional(readOnly = true)
    public List<TicketCategoryResponseDto> findAll() {
        return categoryRepository.findAll().stream()//Buscamos todos los TicketCategory
        .map(categoryMapper::toResponseDto)//Transformamos los TicketCategory a TicketCategoryResponseDto
        .toList();//Los ponemos todos en una lista

    }

    

    @Override
    @Transactional(readOnly = true)
    public List<TicketCategoryResponseDto> getAllByEvent(Long eventId) {

        return categoryRepository.findByEventId(eventId).stream() // Requiere este método en tu Repository porque solo buscamos los de un evento en concreto
                .map(categoryMapper::toResponseDto)//Recorremos como bucle y vamos tranformandolos todos en dto
                .toList();//los ponemos todos en una lista

    }



    @Override
    @Transactional(readOnly = true)
    public TicketCategoryResponseDto findById(Long id) {
        TicketCategory ticketCategory=  categoryRepository.findById(id)
            .orElseThrow(()->new EntityNotFoundException("Categoria no encontrada"));
           return categoryMapper.toResponseDto(ticketCategory);

    }

    @Override
    public TicketCategoryResponseDto create(Long eventId, TicketCategoryCreateDto createDto) {
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("No existe el evento con id " + eventId);
        }
        TicketCategory tkCategory = categoryMapper.toEntity(createDto);
        tkCategory.setEvent(eventRepository.getReferenceById(eventId)); // Proxy ligero, ya sabemos que existe
        return categoryMapper.toResponseDto(categoryRepository.save(tkCategory));



    }

    //Recibe el id de ticketcategory y el dto que se quiere actualizar
    @Override
    @Transactional(readOnly = true)
    public TicketCategoryResponseDto update(Long id, TicketCategoryUpdateDto updateDto) {
      
        // //Buscamos la entidad por la id
        // Ticket_category tkCategory =categoryRepository.findById(id)
        //  .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        // //Transformamos el dto en un tipo ticket category y lo guarda en la variable tkCategory
        // categoryMapper.updateEntityFromDto(updateDto, tkCategory);
        // //Con MapStruct vuelcas los cambios del DTO a la entidad respetando los nulos y reenvia el dto de la tkCategory
        // return categoryMapper.toResponseDto(categoryRepository.save(tkCategory));

        //Lo mismo de arriba mas corto
        return categoryRepository.findById(id).map(tkCategory->{
            //Transformamos el dto en un tipo ticket category y lo guarda en la variable tkCategory
            categoryMapper.updateEntityFromDto(updateDto, tkCategory);
            //Con MapStruct vuelcas los cambios del DTO a la entidad respetando los nulos y reenvia el dto de la tkCategory
            categoryRepository.save(tkCategory);
            return categoryMapper.toResponseDto(tkCategory);
        }).orElseThrow(()->new EntityNotFoundException("No existe el Evento buscado"));    
    }

    @Override
    @Transactional(readOnly = true)
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoría no encontrada");
        }
        categoryRepository.deleteById(id);
    
    }

}
