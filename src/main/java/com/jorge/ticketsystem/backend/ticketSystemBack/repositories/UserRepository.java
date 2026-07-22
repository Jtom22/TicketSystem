package com.jorge.ticketsystem.backend.ticketSystemBack.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Event;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.User;

public interface UserRepository extends JpaRepository <User,Long> {

    Optional<User> findByEmail(String email);

}
