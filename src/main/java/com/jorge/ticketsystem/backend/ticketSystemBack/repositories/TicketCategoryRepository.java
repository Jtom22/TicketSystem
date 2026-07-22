package com.jorge.ticketsystem.backend.ticketSystemBack.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.TicketCategory;

// Usar optional solo en casos que devuelve un solo registro
public interface TicketCategoryRepository extends JpaRepository <TicketCategory,Long> {

     //Modificar para hacer pageable
     List<TicketCategory> findByEventId(Long eventId);

}
