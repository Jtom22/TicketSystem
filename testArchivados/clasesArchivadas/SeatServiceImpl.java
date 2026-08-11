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
public class SeatServiceImpl implements SeatService {

    private final SeatMapperImpl seatMapper;
    private final SeatRepository seatRepository;
    private final TicketCategoryRepository ticketCategoryRepository;

    @Override
    @Transactional
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

        // 4.1. Confirmamos el asiento: pasa de reservado temporalmente a vendido.
        // Ya no hace falta el vínculo con la Order de la reserva (reservedByOrder
        // era solo para saber "quién lo tenía mientras estaba en RESERVADO_TEMPORAL";
        // una vez OCUPADO, el vínculo real y permanente es este propio IssuedTicket).
        asiento.setStatus(SeatStatus.OCUPADO);
        asiento.setReservedByOrder(null);
        seatRepository.save(asiento);

        // 5. Guardamos de forma segura en MySQL
        IssuedTicket guardado = issuedTicketRepository.save(ticketEntity);

        // 6. Retornamos el DTO de respuesta estructurado como Record
        return issuedTicketMapper.toResponseDto(guardado);
    }

    @Override
    public void delete(Long id) {

        seatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe entidad con id " + id));

        seatRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SeatResponseDto> findAll() {

        return seatRepository.findAll().stream()// Con stream indicamos que separamos la lista
                .map(seat -> seatMapper.toResponseDto(seat))// Cada uno de ellos lo convertimos al formato buscado
                .toList();// Java 16+: crea una lista inmutable de forma más eficiente

        // Otra opcion
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
        // Opcion1
        // Seat seat= seatRepository.findById(id)
        // .orElseThrow(()->new EntityNotFoundException("Entidad no encontrada"));
        // return seatMapper.toResponseDto(seat);

        // Opcion2
        return seatRepository.findById(id)
                .map(seatMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Asiento no encontrado con ID: " + id));

    }

    @Override
    public SeatResponseDto update(Long id, SeatUpdateDto updateDto) {
        return seatRepository.findById(id)
                .map(seat -> {
                    seatMapper.updateEntityFromDto(updateDto, seat);
                    seatRepository.save(seat);
                    return seatMapper.toResponseDto(seat);
                })
                .orElseThrow(() -> new EntityNotFoundException("Asiento no encontrado con ID: " + id));

    }

}
