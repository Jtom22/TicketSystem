package com.jorge.ticketsystem.backend.ticketSystemBack.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.user.UserUpdateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.Role;
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.User;
import com.jorge.ticketsystem.backend.ticketSystemBack.mappers.UserMapper;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.RoleRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.UserRepository;
import com.jorge.ticketsystem.backend.ticketSystemBack.services.UserServiceImpl;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

/**
 * El foco de estos tests es la contraseña: en un sistema de autenticación,
 * el bug más caro es guardarla en texto plano por accidente. Estos tests
 * lo comprueban explícitamente en los 3 caminos donde se toca (creación,
 * reactivación de cuenta, y actualización) — es justo el tipo de test que
 * habría atrapado la regresión que encontramos al escribir esta suite.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_usuarioNuevo_hasheaLaPasswordAntesDeGuardar() {
        UserCreateDto dto = new UserCreateDto("Jorge Test", "jorge@test.com", "123456", List.of(2L));
        Role rolUser = new Role();
        rolUser.setId(2L);
        rolUser.setName("USER");

        User nuevoUsuario = new User();
        UserResponseDto responseDto = new UserResponseDto(1L, "Jorge Test", "jorge@test.com", true, List.of("USER"));

        when(userRepository.findByEmail("jorge@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findAllById(List.of(2L))).thenReturn(List.of(rolUser));
        when(userMapper.toEntity(dto)).thenReturn(nuevoUsuario);
        when(passwordEncoder.encode("123456")).thenReturn("HASH-SIMULADO");
        when(userRepository.save(nuevoUsuario)).thenReturn(nuevoUsuario);
        when(userMapper.toResponseDto(nuevoUsuario)).thenReturn(responseDto);

        userService.createUser(dto);

        // La comprobación clave: lo que se guarda en BD es el hash, NUNCA
        // la contraseña en texto plano que mandó el cliente.
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("HASH-SIMULADO");
        assertThat(userCaptor.getValue().getPassword()).isNotEqualTo("123456");
    }

    @Test
    void createUser_emailYaRegistradoYActivo_lanzaEntityExistsException() {
        UserCreateDto dto = new UserCreateDto("Jorge Test", "jorge@test.com", "123456", List.of(2L));
        User usuarioExistente = new User();
        usuarioExistente.setEnabled(true);

        when(userRepository.findByEmail("jorge@test.com")).thenReturn(Optional.of(usuarioExistente));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(EntityExistsException.class);
    }

    @Test
    void createUser_emailExistePeroInactivo_reactivaLaCuentaConPasswordHasheada() {
        // Este es el caso que tuvo la regresión: alguien se había dado de
        // baja, y al "registrarse" otra vez con el mismo email, se reactiva
        // su cuenta — la nueva contraseña TAMBIÉN debe quedar hasheada aquí,
        // no solo en el camino de usuario 100% nuevo.
        UserCreateDto dto = new UserCreateDto("Jorge Reactivado", "jorge@test.com", "nuevaPass123", List.of(2L));
        Role rolUser = new Role();
        rolUser.setId(2L);

        User usuarioInactivo = new User();
        usuarioInactivo.setEnabled(false);

        when(userRepository.findByEmail("jorge@test.com")).thenReturn(Optional.of(usuarioInactivo));
        when(roleRepository.findAllById(List.of(2L))).thenReturn(List.of(rolUser));
        when(passwordEncoder.encode("nuevaPass123")).thenReturn("HASH-DE-LA-NUEVA");
        when(userRepository.save(usuarioInactivo)).thenReturn(usuarioInactivo);
        when(userMapper.toResponseDto(usuarioInactivo))
                .thenReturn(new UserResponseDto(1L, "Jorge Reactivado", "jorge@test.com", true, List.of("USER")));

        userService.createUser(dto);

        assertThat(usuarioInactivo.isEnabled()).isTrue(); // se reactivó
        assertThat(usuarioInactivo.getPassword()).isEqualTo("HASH-DE-LA-NUEVA"); // y quedó hasheada
        assertThat(usuarioInactivo.getPassword()).isNotEqualTo("nuevaPass123");
    }

    @Test
    void updateUser_cuandoNoExiste_lanzaEntityNotFoundException() {
        UserUpdateDto dto = new UserUpdateDto("Nuevo Nombre", null, null, null);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateUser_conPasswordNueva_laHasheaAntesDeGuardar() {
        UserUpdateDto dto = new UserUpdateDto(null, "otraPassword", null, null);
        User usuarioExistente = new User();

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.encode("otraPassword")).thenReturn("HASH-NUEVO");
        when(userRepository.save(usuarioExistente)).thenReturn(usuarioExistente);
        when(userMapper.toResponseDto(usuarioExistente))
                .thenReturn(new UserResponseDto(1L, null, null, true, null));

        userService.updateUser(1L, dto);

        assertThat(usuarioExistente.getPassword()).isEqualTo("HASH-NUEVO");
    }

    @Test
    void updateUser_sinPasswordEnElDto_noTocaLaPasswordExistente() {
        // password es opcional en el PATCH — si no viene, la contraseña
        // actual debe quedar intacta, no debe llamarse al encoder.
        UserUpdateDto dto = new UserUpdateDto("Solo cambio el nombre", null, null, null);
        User usuarioExistente = new User();
        usuarioExistente.setPassword("HASH-ANTIGUO-SIN-TOCAR");

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(userRepository.save(usuarioExistente)).thenReturn(usuarioExistente);
        when(userMapper.toResponseDto(usuarioExistente))
                .thenReturn(new UserResponseDto(1L, "Solo cambio el nombre", null, true, null));

        userService.updateUser(1L, dto);

        assertThat(usuarioExistente.getPassword()).isEqualTo("HASH-ANTIGUO-SIN-TOCAR");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deleteUser_hacBorradoLogico_noBorraDeVerdad() {
        User usuario = new User();
        usuario.setEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));

        userService.deleteUser(1L);

        assertThat(usuario.isEnabled()).isFalse(); // se desactiva...
        verify(userRepository).save(usuario);        // ...pero se GUARDA, no se borra
        verify(userRepository, never()).delete(any());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUser_cuandoNoExiste_lanzaEntityNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
