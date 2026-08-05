package com.jorge.ticketsystem.backend.ticketSystemBack.entities;



import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
// @NoArgsConstructor // Crea el constructor vacío obligatorio para JPA
// @AllArgsConstructor // Crea un constructor con todos los campos (útil para pruebas)
// @Builder // Te permite usar el patrón Seat.builder() más adelante
public class User extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String full_name;
    private String email;
    private String password;
    private boolean enabled;


@ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles", // Nombre de la tabla intermedia
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"),
        uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "role_id" }) }
    )
    private List<Role> roles;
    

    // Puedes inicializarla en el constructor para evitar NullPointerException
    public User() {
        this.roles = new ArrayList<>();
    }

    // Replace getRole()/setRole() por getRoles()/setRoles()
    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }



}
