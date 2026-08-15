package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.issuedTicket.IssuedTicketUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.IssuedTicket;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Order;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Seat;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.SeatStatus;
import com.jorge.ticketsystem.backend.ticketSystemBack.exception.SeatUnavailableException;
import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.IssuedTicketMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.IssuedTicketRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.OrderRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.SeatRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssuedTicketServiceImpl implements IssuedTicketService {

    private final IssuedTicketRepository issuedTicketRepository;

    private final IssuedTicketMapper issuedTicketMapper;
    private final OrderRepository orderRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional(readOnly = true)
    public IssuedTicketResponseDto update(Long id, IssuedTicketUpdateDto dto) {

        return issuedTicketRepository.findById(id).map(issuedTicket -> {
            // MapStruct actualiza la entidad existente ignorando los valores null del DTO
            issuedTicketMapper.updateEntityFromDto(dto, issuedTicket);
            // Guardamos la entidad modificada y la devolvemos envuelta en el Optional
            // Devuelve el mismo DTO de actualización envuelto en un Optional (según tu
            // interfaz)
            return issuedTicketMapper.toResponseDto(issuedTicketRepository.save(issuedTicket));
        }).orElseThrow(() -> new EntityNotFoundException("No existe el issued ticket de id: " + id));

    }

    @Override
    @Transactional(readOnly = true)
    public IssuedTicketResponseDto create(IssuedTicketCreateDto dto) {
        // 1. Buscamos y validamos la Orden en su repositorio
        Order orden = orderRepository.findById(dto.orderId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se puede emitir el tickets: La Orden con ID " + dto.orderId() + " no existe."));

        // 2. Buscamos y validamos el Asiento en su repositorio
        Seat asiento = seatRepository.findById(dto.seatId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se puede emitir el tickets: El Asiento con ID " + dto.seatId() + " no existe."));

        // 2.1. El asiento tiene que estar reservado (por ESTA orden) antes de poder
        // emitir su ticket. Si está DISPONIBLE, nadie pasó por el paso de reserva.
        // Si está OCUPADO, ya se emitió un ticket para él antes (no se puede repetir).
        if (asiento.getStatus() != SeatStatus.RESERVADO_TEMPORAL) {
            throw new SeatUnavailableException(asiento.getId());
        }

        // 2.1.b Comprobamos la fecha real de expiración, no solo el status.
        // El @Scheduled que libera reservas caducadas corre cada minuto — sin esto,
        // habría una ventana de hasta 60s en la que la reserva ya venció de verdad
        // pero el status en BD seguía diciendo RESERVADO_TEMPORAL, dejando colarse
        // una compra fuera de plazo. actualmente son 10 min lo que tienes para realizar la compra
        if (asiento.getReservedByOrder() != null
                && asiento.getReservedByOrder().getExpires_at().isBefore(java.time.LocalDateTime.now())) {
            throw new SeatUnavailableException(asiento.getId());
        }

        // 2.2. Comprobamos que la reserva pertenece a la MISMA orden que está
        // emitiendo el ticket, no a otra. Sin esto, alguien podría emitir un
        // ticket sobre un asiento que otra persona tiene reservado ahora mismo.
        if (asiento.getReservedByOrder() == null
                || !asiento.getReservedByOrder().getId().equals(orden.getId())) {
            throw new SeatUnavailableException(asiento.getId());
        }

        // 3. Convertimos el DTO a Entidad mediante MapStruct
        IssuedTicket ticketEntity = issuedTicketMapper.toEntity(dto);

        // 4. Inyectamos los objetos relacionales validados a nuestra entidad
        ticketEntity.setOrder(orden);
        ticketEntity.setSeat(asiento);
        issuedTicketRepository.save(ticketEntity);

        // 5. Guardamos de forma segura en MySQL
        IssuedTicket guardado = issuedTicketRepository.save(ticketEntity);

        // 6. Retornamos el DTO de respuesta estructurado como Record
        return issuedTicketMapper.toResponseDto(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public void delete(Long id) {
        if (!issuedTicketRepository.existsById(id)) {
            throw new EntityNotFoundException("No se puede eliminar, boleto no encontrado");
        }
        issuedTicketRepository.deleteById(id);

    }

    @Override
    @Transactional(readOnly = true)
    public IssuedTicketResponseDto getTicketById(Long id) {
        IssuedTicket ticket = issuedTicketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tickets no encontrado con ID: " + id));
        return issuedTicketMapper.toResponseDto(ticket);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<IssuedTicketResponseDto> getTicketBySeat(Long seatId, Pageable pageable) {

        // 2. Buscamos y validamos el Asiento en su repositorio
        if (!seatRepository.existsById(seatId)) {
            throw new EntityNotFoundException("El Asiento con ID " + seatId + " no existe.");
        }
        // 2. Si existe, procedemos con la consulta paginada de forma segura
        Page<IssuedTicket> entidadesPage = issuedTicketRepository.findBySeatId(seatId, pageable);
        return entidadesPage.map(issuedTicketMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IssuedTicketResponseDto> getTicketByOrder(Long orderId, Pageable pageable) {
        // TODO Auto-generated method stub
        if (!orderRepository.existsById(orderId)) {
            throw new EntityNotFoundException("La orden con ID" + orderId + "no existe");
        }
        Page<IssuedTicket> entidadesPage = issuedTicketRepository.findByOrderId(orderId, pageable);
        return entidadesPage.map(issuedTicketMapper::toResponseDto);
        // Es lo mismo de arriba
        // entidadesPage.map(entidad -> issuedTicketMapper.toResponseDto(entidad))
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IssuedTicketResponseDto> getAllTickets(Pageable pageable) {
        return issuedTicketRepository.findAll(pageable).map(issuedTicketMapper::toResponseDto);

    }

}
