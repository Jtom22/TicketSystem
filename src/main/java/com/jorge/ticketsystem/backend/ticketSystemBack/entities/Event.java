package com.jorge.ticketsystem.backend.ticketSystemBack.entities;


import java.time.LocalDateTime;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="events")
@Getter
@Setter
@NoArgsConstructor
public class Event extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Long id;
    private String title;
    private String artist;
    private String venue_name;
    private String city;
    private LocalDateTime event_date;

}
