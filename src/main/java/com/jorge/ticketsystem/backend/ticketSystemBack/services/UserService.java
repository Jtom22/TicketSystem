package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserUpdateDto;

public interface UserService {

    UserResponseDto createUser(UserCreateDto dto);

    UserResponseDto updateUser(Long id, UserUpdateDto dto);

    Page<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto getUserByEmail(String email);
    
    void deleteUser(Long id);


}
