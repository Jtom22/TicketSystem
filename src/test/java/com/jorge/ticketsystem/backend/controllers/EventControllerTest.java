package com.jorge.ticketsystem.backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import com.jorge.ticketsystem.backend.ticketSystemBack.TicketSystemBackApplication;
import com.jorge.ticketsystem.backend.ticketSystemBack.controllers.EventController;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Event;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.EventService;

import tools.jackson.databind.ObjectMapper;


@WebMvcTest(controllers = EventController.class)
@ContextConfiguration(classes = TicketSystemBackApplication.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simula las peticiones HTTP externas

    @MockitoBean // Inyecta el simulador del servicio en el contenedor de Spring
    private EventService service;

 // CORREGIDO: Convierte objetos Java a texto JSON nativamente
     @Autowired
    private ObjectMapper objectMapper;

    private EventResponseDto responseDto;
    private Event eventEntity;

    @BeforeEach
    void setUp() {
        // CORREGIDO: Inicialización exacta con 5 parámetros (sin ID) adaptada a tu DTO real
        responseDto = new EventResponseDto(
            "Concierto de Rock",     
            "Banda de Rock Local",   
            "Estadio Nacional",      
            "Madrid",                
            LocalDateTime.now().plusDays(10) 
        );

        // La entidad en la base de datos mantiene su ID interno Long obligatorio
        eventEntity = new Event();
        eventEntity.setId(1L);
        eventEntity.setTitle("Concierto de Rock");
        eventEntity.setArtist("Banda de Rock Local");
        eventEntity.setVenue_name("Estadio Nacional");
        eventEntity.setCity("Madrid");
        eventEntity.setEvent_date(responseDto.eventDate());
    }

    @Test
    void getById_ShouldReturnEvent_WhenExists() throws Exception {
        // GIVEN: El servicio retorna tu DTO sin ID
        when(service.findById(1L)).thenReturn(Optional.of(responseDto));

        // WHEN & THEN: Evaluamos las propiedades reales que viajan en el JSON
        mockMvc.perform(get("/events/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.id").doesNotExist()) // Senior: Valida que el ID NO esté expuesto en la salida
                .andExpect(jsonPath("$.title").value("Concierto de Rock"))
                .andExpect(jsonPath("$.artist").value("Banda de Rock Local"))
                .andExpect(jsonPath("$.city").value("Madrid"));
    }

     @Test
    void create_ShouldReturnCreatedEvent_WhenValid() throws Exception {
        when(service.save(any(EventCreateDto.class))).thenReturn(eventEntity);

        // Bloque de texto plano nativo de Java para evitar usar variables ObjectMapper externas
        String createDtoJson = """
            {
                "title": "Concierto de Rock",
                "artist": "Banda de Rock Local",
                "venueName": "Estadio Nacional",
                "city": "Madrid",
                "eventDate": "2026-07-18T20:00:00"
            }
            """;

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createDtoJson)) 
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.id").value(1)) 
                .andExpect(jsonPath("$.title").value("Concierto de Rock"));
    }
}
