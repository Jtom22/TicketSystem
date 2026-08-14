package com.jorge.ticketsystem.backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.controllers.EventController;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.EventService;


//  Test de la capa CONTROLLER. Aquí SÍ importa la ruta HTTP exacta, los
//  códigos de estado, y que @Valid rechace datos incorrectos — el servicio
//  va mockeado, no probamos su lógica interna aquí (para eso está
//  EventServiceImplTest).
// 
//  @AutoConfigureMockMvc(addFilters = false) desactiva la cadena de filtros
//  de Spring Security para este test — probamos el controller en aislado,
//  sin necesitar montar JwtAuthenticationFilter/SecurityConfig enteros.
 
@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(EventController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    @Test
    void getAllEvents_devuelve200ConLaLista() throws Exception {
        EventResponseDto dto = new EventResponseDto(
                1L, "Concierto", "Artista", "Sala", "Zaragoza", LocalDateTime.now().plusDays(10));

        // Convertimos la lista en una Page usando PageImpl        
        Page<EventResponseDto> page = new PageImpl<>(List.of(dto));
        // Usamos any(Pageable.class) para aceptar cualquier configuración de paginación que envíe Spring
        when(eventService.findAll(any(Pageable.class))).thenReturn(page);//Devolvemos lista con el dto

        mockMvc.perform(get("/api/v1/events")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk()) // Comprobamos que devuelve un 200 OK
            .andExpect(jsonPath("$.content[0].title").value("Concierto")) // Accedemos a la lista dentro de "content"
            .andExpect(jsonPath("$.totalElements").value(1)); // (Opcional) Comprobamos el total de elementos

    }

    @Test
    void getById_devuelve200ConElEvento() throws Exception {
        EventResponseDto dto = new EventResponseDto(
                1L, "Concierto", "Artista", "Sala", "Zaragoza", LocalDateTime.now().plusDays(10));
        when(eventService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_conBodyValido_devuelve201() throws Exception {
        EventCreateDto createDto = new EventCreateDto(
                "Concierto de Rock", "The Killers", "Auditorio", "Zaragoza", LocalDateTime.now().plusMonths(1));
        EventResponseDto responseDto = new EventResponseDto(
                1L, "Concierto de Rock", "The Killers", "Auditorio", "Zaragoza", createDto.eventDate());

        when(eventService.create(any(EventCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Concierto de Rock"));
    }

    @Test
    void create_conTituloVacio_devuelve400YNoLlamaAlServicio() throws Exception {
        // title en blanco viola @NotBlank de EventCreateDto — @Valid debe
        // rechazarlo ANTES de que el controller llame al service.
        String jsonInvalido = """
                {"title":"","artist":"The Killers","venueName":"Auditorio","city":"Zaragoza","eventDate":"2027-06-15T21:00:00"}
                """;

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
    }

    @Test
    void create_conFechaEnElPasado_devuelve400() throws Exception {
        // @Future en eventDate — una fecha pasada debe rechazarse por validación
        String jsonInvalido = """
                {"title":"Concierto","artist":"The Killers","venueName":"Auditorio","city":"Zaragoza","eventDate":"2020-01-01T21:00:00"}
                """;

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }
}