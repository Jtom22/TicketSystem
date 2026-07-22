package com.jorge.ticketsystem.backend.ticketSystemBack.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Order;

public interface OrderRepository extends JpaRepository<Order,Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

}
