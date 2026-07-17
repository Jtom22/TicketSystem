package com.jorge.ticketsystem.backend.ticketSystemBack.entities;

import java.time.LocalDateTime;



import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter

public class Order extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int total_amount;
    private String status;
    private LocalDateTime expires_at;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable= false)
    private User user;



}
