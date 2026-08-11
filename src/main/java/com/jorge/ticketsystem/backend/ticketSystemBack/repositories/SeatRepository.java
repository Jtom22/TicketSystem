package com.jorge.ticketsystem.backend.ticketSystemBack.repositories;



import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Seat;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.SeatStatus;

public interface SeatRepository extends JpaRepository <Seat, Long>{

    Page<Seat> findByTicketCategoryId(Long categoryId, Pageable pageable);

    // Asientos que siguen "reservados" cuya Order ya pasó su fecha de expiración.
    // Usamos @Query (JPQL) en vez de un método derivado porque el campo de la entidad
    // se llama "expires_at" con guion bajo, y Spring Data lo interpretaría mal si
    // intentáramos escribirlo como findByStatusAndReservedByOrder_ExpiresAtBefore(...).
    @Query("SELECT s FROM Seat s WHERE s.status = :status AND s.reservedByOrder.expires_at< :now")
    List<Seat> findExpiredReservedSeats(@Param("status") SeatStatus status, @Param("now") LocalDateTime now);
}
