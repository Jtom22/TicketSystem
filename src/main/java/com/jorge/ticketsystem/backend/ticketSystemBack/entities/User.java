package com.jorge.ticketsystem.backend.ticketSystemBack.entities;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter // Genera getters para todas las variables (incluyendo el id)
@Setter // Genera setters para todas las variables
@NoArgsConstructor // Crea el constructor vacío obligatorio para JPA
@AllArgsConstructor // Crea un constructor con todos los campos (útil para pruebas)
@Builder // Te permite usar el patrón Seat.builder() más adelante
public class User extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String full_name;
    private String email;
    private String password;
    private boolean enabled;


    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;



}
