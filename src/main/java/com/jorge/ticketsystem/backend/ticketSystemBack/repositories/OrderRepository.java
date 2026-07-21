package com.jorge.ticketsystem.backend.ticketSystemBack.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Order;

public interface OrderRepository extends JpaRepository<Order,Long> {

    

}
