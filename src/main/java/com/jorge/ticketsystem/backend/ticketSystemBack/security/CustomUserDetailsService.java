package com.jorge.ticketsystem.backend.ticketSystemBack.security;

import java.util.List;
 
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
 
import com.jorge.ticketsystem.backend.ticketSystemBack.entities.User;
import com.jorge.ticketsystem.backend.ticketSystemBack.repositories.UserRepository;
 
import lombok.RequiredArgsConstructor;
 
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
 
    private final UserRepository userRepository;
 
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No existe usuario con email: " + email));
 
        List<GrantedAuthority> authorities = user.getRoles().stream()
                // Spring Security espera el prefijo "ROLE_" para que hasRole("ADMIN") funcione.
                // Tu Role.name guarda solo "ADMIN"/"USER", así que lo añadimos aquí.
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .map(GrantedAuthority.class::cast)
                .toList();
 
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // ya viene hasheado desde UserServiceImpl
                .authorities(authorities)
                .disabled(!user.isEnabled()) // reutiliza tu borrado lógico (enabled = false)
                .build();
    }
}
