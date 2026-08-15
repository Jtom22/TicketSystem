package com.jorge.ticketsystem.backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.jorge.ticketsystem.backend.ticketSystemBack.controllers.OrderController;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.order.OrderResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.exception.SeatUnavailableException;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.OrderServiceImpl;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(OrderController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // OrderController inyecta la clase concreta OrderServiceImpl (no una
    // interfaz), así que mockeamos ese mismo tipo aquí.
    @MockitoBean
    private OrderServiceImpl orderService;

    @Test
    void createOrder_conBodyValido_devuelve201() throws Exception {
        OrderCreateDto createDto = new OrderCreateDto(3L, List.of(12L, 13L));
        OrderResponseDto responseDto = new OrderResponseDto(
                1L, 45, "Pending", LocalDateTime.now().plusMinutes(10), 3L);

        when(orderService.createOrder(any(OrderCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("Pending"));
    }

    @Test
    void createOrder_sinAsientosSeleccionados_devuelve400() throws Exception {
        // seatIds vacío viola @NotEmpty de OrderCreateDto
        String jsonInvalido = """
                {"userId":3,"seatIds":[]}
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void createOrder_cuandoElServicioLanzaSeatUnavailable_devuelve409() throws Exception {
        OrderCreateDto createDto = new OrderCreateDto(3L, List.of(12L));

        when(orderService.createOrder(any(OrderCreateDto.class)))
                .thenThrow(new SeatUnavailableException(12L));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void getOrdersByUserId_devuelve200ConLaPagina() throws Exception {
        OrderResponseDto responseDto = new OrderResponseDto(
                1L, 45, "Pending", LocalDateTime.now().plusMinutes(10), 3L);
        Page<OrderResponseDto> page = new PageImpl<>(List.of(responseDto));

        when(orderService.getOrdersByUserId(eq(3L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users/3/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(3));
    }
}