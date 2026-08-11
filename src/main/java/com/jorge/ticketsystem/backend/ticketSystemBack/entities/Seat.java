package com.jorge.ticketsystem.backend.ticketSystemBack.entities;




import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="seats")
@Getter
@Setter
@NoArgsConstructor
public class Seat extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String seat_number;
    
    @Enumerated(EnumType.STRING)
    private SeatStatus status= SeatStatus.DISPONIBLE;
    
    @Version
    private int version;

    
    @ManyToOne
    @JoinColumn(name = "category_id", nullable=false)
    private TicketCategory ticketCategory;

    // Qué Order tiene ahora mismo la reserva temporal de este asiento.
    // null cuando el asiento está DISPONIBLE.
    @ManyToOne
    @JoinColumn(name = "reserved_by_order_id")
    private Order reservedByOrder;

}
