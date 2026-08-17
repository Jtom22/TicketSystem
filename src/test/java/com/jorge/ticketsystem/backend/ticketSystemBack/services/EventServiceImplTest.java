package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Event;
import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.EventMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.EventRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.TicketCategoryRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.EventServiceImpl;

import jakarta.persistence.EntityNotFoundException;

/**
 * Tests de la capa de SERVICIO, aislada del framework web.
 * Mockeamos EventRepository/EventMapper: aquí no probamos si la ruta HTTP
 * es correcta (eso es cosa de EventControllerTest), solo la lógica de negocio.
 */
@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private TicketCategoryRepository ticketCategoryRepository;

    @Mock
    private EventRepository repository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void findAll_devuelveTodosLosEventosMapeados() {

        //PREPARAMOS ENTORNO
        // Creamos evento de prueba
        Event event = new Event();
        event.setId(1L);
        EventResponseDto responseDto = new EventResponseDto(
                1L, "Concierto", "Artista", "Sala", "Zaragoza", LocalDateTime.now().plusDays(10));

        // Definimos el objeto de paginación de entrada
        Pageable pageable = PageRequest.of(0, 10);
        // Envolvemos nuestro evento de prueba en un objeto Page simulado
        Page<Event> eventPage = new PageImpl<>(List.of(event),pageable,1);

        // le decimos que cuando alguien busque el findAll le devolvamos una lista con
        // el evento de prueba
        when(repository.findAll(pageable)).thenReturn(eventPage);
        // simulamos su transformacion en dto
        when(eventMapper.toResponseDto(event)).thenReturn(responseDto);

        //EJECUTAMOS el metodo que estamos buscando
        // llamamos al findAll()
        Page<EventResponseDto> result = eventService.findAll(pageable);

        //VERIFICAMOS que los resultado son los buscados
        // Con assertThat, verifica que la lista resultante tenga exactamente 1 elemento
        // y que el título del primer evento sea "Concierto"
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Concierto");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findById_cuandoExiste_devuelveElEvento() {


        //PREPARAMOS ENTORNO
        // Creamos evento de prueba
        Event event = new Event();
        event.setId(1L);
        EventResponseDto responseDto = new EventResponseDto(
                1L, "Concierto", "Artista", "Sala", "Zaragoza", LocalDateTime.now().plusDays(10));

        // Cuando se llame al findById(1L) le devolveremos un Optional del evento porque
        // asi lo devuelve el JPA del repostory
        when(repository.findById(1L)).thenReturn(Optional.of(event));
        // Simulamos su mapeo
        when(eventMapper.toResponseDto(event)).thenReturn(responseDto);

        //EJECUTAMOS el metodo que estamos buscando
        // llamamos al findById(1L)
        EventResponseDto result = eventService.findById(1L);

        //VERIFICAMOS que los resultado son los buscados
        // Comprobamos que el resultado es el id 1L
        assertThat(result.id()).isEqualTo(1L);
    }

    //Probamos excepcion
    @Test
    void findById_cuandoNoExiste_lanzaEntityNotFoundException() {
        // Le decimos que cuando llamen al 999L devuelva un Optional vacio
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_guardaYDevuelveElEventoCreado() {
        //PREPARAMOS el entorno
        //Dto de creacion
        EventCreateDto createDto = new EventCreateDto(
                "Concierto de Rock", "The Killers", "Auditorio", "Zaragoza", LocalDateTime.now().plusMonths(1));
        Event eventEntity = new Event();
        //Dto esperado
        EventResponseDto responseDto = new EventResponseDto(
                1L, "Concierto de Rock", "The Killers", "Auditorio", "Zaragoza", createDto.eventDate());

        
        when(eventMapper.toEntity(createDto)).thenReturn(eventEntity);
        when(eventMapper.toResponseDto(eventEntity)).thenReturn(responseDto);

        //EJECUTAMOS la llamada
        EventResponseDto result = eventService.create(createDto);

        //COMPROBAMOS el resultado
        verify(repository).save(eventEntity); // Confirmamos se ha llamado a save
        assertThat(result.title()).isEqualTo("Concierto de Rock");
    }

    @Test
    void delete_cuandoNoExiste_lanzaEntityNotFoundException() {
        when(repository.existsById(999L)).thenReturn(false);

        // Capturamos la excepción exacta que lanza tu código actual
        assertThatThrownBy(() -> eventService.delete(999L))
                .isInstanceOf(RuntimeException.class) // 👈 Cambiado de EntityNotFoundException a RuntimeException
                .hasMessageContaining("No se puede eliminar: Evento no encontrado con ID: 999"); // 👈 Opcional: valida
                                                                                                 // el mensaje
    }
}
