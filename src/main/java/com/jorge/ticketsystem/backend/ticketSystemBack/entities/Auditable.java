package com.jorge.ticketsystem.backend.ticketSystemBack.entities;


import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass // Le dice a JPA que no cree una tabla para esta clase, sino que comparta sus campos
@EntityListeners(AuditingEntityListener.class) // Activa el "escuchador" automático de Spring Data
@NoArgsConstructor
public abstract class Auditable {

    @CreatedDate // Spring mete la fecha actual automáticamente al insertar el registro
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // Spring actualiza esta fecha automáticamente al modificar el registro
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- GETTERS Y SETTERS ---
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
