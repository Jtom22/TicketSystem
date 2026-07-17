package com.jorge.ticketsystem.backend.ticketSystemBack.repositories;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Seat;

public interface SeatRepository extends JpaRepository <Seat, Long>{

    Page<Seat> findByTicketCategoryId(Long categoryId, Pageable pageable);
}
