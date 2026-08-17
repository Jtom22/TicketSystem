package com.jorge.ticketsystem.backend.ticketSystemBack.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.IssuedTicket;

public interface IssuedTicketRepository extends JpaRepository <IssuedTicket,Long> {


    // Devuelve una página de boletos para un asiento (Filtrado y paginado en MySQL con LIMIT y OFFSET)
    Page<IssuedTicket> findBySeatId(Long seatId, Pageable pageable);

     // Devuelve una página de boletos para una orden
    Page<IssuedTicket> findByOrderId(Long orderId, Pageable pageable);
    
    // SELECT * FROM issued_tickets WHERE qr_code_token = ?
    Optional<IssuedTicket> findByQrCodeToken(String qr_code_token);
}
