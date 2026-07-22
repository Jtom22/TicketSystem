package com.jorge.ticketsystem.backend.ticketSystemBack.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 1. CREAR O REACTIVAR UN USUARIO
     * POST http://localhost:8080/api/v1/users
     */
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    /**
     * 2. ACTUALIZACIÓN PARCIAL DE UN USUARIO (PATCH)
     * URL: PATCH http://localhost:8080/api/v1/users/1
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    /**
     * 3. LISTAR TODOS LOS USUARIOS ACTIVOS PAGINADOS
     * GET http://localhost:8080/api/v1/users?page=0&size=10&sort=full_name,asc
     */
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 10, sort = "full_name") Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    /**
     * 4. BUSCAR UN ÚNICO USUARIO POR EMAIL
     * GET http://localhost:8080/api/v1/users/search?email=juan@gmail.com
     */
    @GetMapping("/search")
    public ResponseEntity<UserResponseDto> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    /**
     * 5. BORRADO LÓGICO DE UN USUARIO (DESACTIVACIÓN)
     * URL: DELETE http://localhost:8080/api/v1/users/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
