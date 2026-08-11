package com.jorge.ticketsystem.backend.ticketSystemBack.entities;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;   
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ticket_categories")
@Getter
@Setter
@NoArgsConstructor
public class TicketCategory extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal price;
    private int capacity;
    
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    


}
