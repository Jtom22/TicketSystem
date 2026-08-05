package com.jorge.ticketsystem.backend.ticketSystemBack.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Role;

@Repository
public interface RoleRepository extends JpaRepository <Role
,Long> {


}
