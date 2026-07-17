package com.jorge.ticketsystem.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Event;
import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.EventMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.EventRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.EventServiceImpl;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {
    // @Mock le dice a Mockito: "Crea un objeto falso de EventRepository esd normal
    // que no cree registro en la bbdd
    @Mock
    private EventRepository repository; // <--- MOCKITO (Crea el clon)
    @Mock
    private EventMapper mapper; // <--- MOCKITO (Crea el clon)
    @InjectMocks
    private EventServiceImpl service; // <--- MOCKITO (Inyecta los clones)

    private EventCreateDto createDto;
    private Event eventEntity;

    @BeforeEach // <--- JUNIT (Ciclo de vida)
    void setUp() {
        // Creamos el evento y su dto antes del test
        createDto = new EventCreateDto(
                "Concierto de Rock",
                "Banda de Rock Local",
                "Estadio Nacional",
                "Madrid",
                LocalDateTime.now().plusDays(10));

        eventEntity = new Event();
        eventEntity.setId(1L);
        eventEntity.setTitle("Concierto de Rock");
        eventEntity.setArtist("Banda de Rock Local");
        eventEntity.setVenue_name("Estadio Nacional");
        eventEntity.setCity("Madrid");
        eventEntity.setEvent_date(createDto.eventDate());
    }

    @Test
    void save_ShouldReturnSavedEvent_WhenSuccessful() {
        // GIVEN
        when(mapper.toEntity(any(EventCreateDto.class))).thenReturn(eventEntity);// <--- MOCKITO (Simula comportamiento)
        when(repository.save(any(Event.class))).thenReturn(eventEntity);// <--- MOCKITO (Simula comportamiento)

        // WHEN
        Event result = service.save(createDto); // <--- TU CÓDIGO REAL

        // THEN
        assertNotNull(result, "El evento guardado no debería ser nulo"); // <--- JUNIT (Comprueba el resultado)
        assertEquals(1L, result.getId());
        assertEquals("Concierto de Rock", result.getTitle());
        assertEquals("Madrid", result.getCity());

        verify(mapper, times(1)).toEntity(createDto);// <--- MOCKITO (Verifica comportamiento)
        verify(repository, times(1)).save(eventEntity);
    }

    @Test
    void update_ShouldReturnUpdatedDto_WhenEventExists() {
        // GIVEN (Preparación del escenario)
        Long eventId = 1L;

        // Creamos el DTO con los nuevos datos que queremos cambiar (El cliente cambia
        // el título y el lugar)
        EventUpdateDto updateDto = new EventUpdateDto(
                "Concierto de Rock AC/DC",
                "Banda de Rock Local",
                "Estadio Metropolitano",
                "Madrid",
                LocalDateTime.now().plusDays(15));

        // Simulamos que el repositorio encuentra el evento original en la base de datos
        when(repository.findById(eventId)).thenReturn(Optional.of(eventEntity));

        // Simulamos el comportamiento del repositorio al salvar los cambios
        when(repository.save(any(Event.class))).thenReturn(eventEntity);

        // WHEN (Ejecución de la acción real)
        Optional<EventUpdateDto> result = service.update(eventId, updateDto);

        // THEN (Verificaciones de JUnit y Mockito)
        // 1. Verificamos con JUnit que el Optional no venga vacío y tenga los datos
        // correctos
        org.junit.jupiter.api.Assertions.assertTrue(result.isPresent(), "El resultado debería contener un valor");
        assertEquals("Concierto de Rock AC/DC", result.get().title());
        assertEquals("Estadio Metropolitano", result.get().venueName());

        // 2. Verificamos con Mockito el comportamiento de las dependencias
        // Comprobamos que se buscó el evento por su ID exacto
        verify(repository, times(1)).findById(eventId);

        // Comprobamos que el Mapper hizo su trabajo de fusionar los datos del DTO en la
        // entidad de la BD
        verify(mapper, times(1)).updateEntityFromDto(updateDto, eventEntity);

        // Comprobamos que se guardaron los cambios una sola vez
        verify(repository, times(1)).save(eventEntity);
    }
}
