package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Role;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.User;
import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.UserMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.RoleRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.UserRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // 1. Inyectamos RoleRepository
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    

    @Transactional
    public UserResponseDto createUser(UserCreateDto dto) {
        
       Optional<User> existingUserOpt = userRepository.findByEmail(dto.email());

       List<Role> roles = roleRepository.findAllById(dto.roleIds());
    if (existingUserOpt.isPresent()) {
        User existingUser = existingUserOpt.get();

        // CASO A: Si está ACTIVO, lanzamos el error de duplicado normal
        if (existingUser.isEnabled()) {
            throw new jakarta.persistence.EntityExistsException("El correo electrónico ya está registrado");
        }

        // CASO B: Si está INACTIVO, lo reactivamos con los nuevos datos recibidos
        existingUser.setFull_name(dto.fullName());
        // existingUser.setPassword(passwordEncoder.encode(dto.password())); // Encriptar en producción
        existingUser.setPassword(dto.password());
        existingUser.setEnabled(true); // ⬅️ Lo volvemos a dar de alta
        existingUser.setRoles(roles);
        return userMapper.toResponseDto(userRepository.save(existingUser));
    }

    // CASO C: Si el correo no existe en absoluto, se crea un usuario nuevo desde cero


    //Hay que modifciar para que las altas normales se diferencien entre las altas con roles admin 
    User newUser = userMapper.toEntity(dto);
    newUser.setPassword(passwordEncoder.encode(dto.password()));
    newUser.setRoles(roles);
    return userMapper.toResponseDto(userRepository.save(newUser));
    
    }

    @Transactional
    //Actualizamos el user desde un dto mediante el mapper
    public UserResponseDto updateUser(Long id, UserUpdateDto dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario con ID: " + id));

        userMapper.updateEntityFromDto(dto, existingUser);
        //Comprobamos que la contraseña no esta en blanco ni es null, de esta manera solo modificamos pswd si nos manda una valida si no se mantiene la antigua
        if (dto.password() != null && !dto.password().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(dto.password()));
        }

        return userMapper.toResponseDto(userRepository.save(existingUser));
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toResponseDto);
    }


    //Buscamos user por email concreto
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email){
        return userMapper.toResponseDto(userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("No existe ningun user con este email: "+ email)));
    }

    //no queremos borrar usuarios nunca, la intencion es modificar los estados
    @Transactional
    public void deleteUser(Long id){
    User user = userRepository.findById(id)
        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("No se encontró el usuario con ID: " + id));
    
    // 2. Cambiamos su estado a inactivo
    user.setEnabled(false);
    
    // 3. Guardamos los cambios
    userRepository.save(user);
    }
} 
