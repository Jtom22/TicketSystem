package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketUpdateDto;

public interface IssuedTicketService {


    // 1. Crear / Emitir un nuevo ticket
    IssuedTicketResponseDto create(IssuedTicketCreateDto dto);

    // 2. Buscar un ticket específico por su ID único
    IssuedTicketResponseDto getTicketById(Long id);

    // 3. Obtener TODOS los tickets del sistema 
    Page<IssuedTicketResponseDto> getAllTickets(Pageable pageable);
    // 4. Obtener todos los tickets comprados en una misma orden
    Page<IssuedTicketResponseDto>getTicketByOrder(Long orderId, Pageable pageable);

    // 5. Obtener el historial de tickets asociados a un asiento
    Page<IssuedTicketResponseDto> getTicketBySeat(Long seatId, Pageable pageable);

    // 6. Actualizar datos de un ticket (como el token del QR si cambia)
    IssuedTicketResponseDto update(Long id, IssuedTicketUpdateDto dto);

    // 7. Eliminar / Cancelar un ticket emitido
    void delete(Long id);


}
