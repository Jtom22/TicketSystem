package com.jorge.ticketsystem.backend.ticketSystemBack.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Event;


public interface EventRepository extends JpaRepository <Event,Long> {


}
