package com.camilo.scorefilm.auth.user.service;

import com.camilo.scorefilm.auth.user.dto.UserResponse;
import com.camilo.scorefilm.auth.user.entity.User;
import com.camilo.scorefilm.auth.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * Obtener usuario por username y convertir a DTO
     */
    public UserResponse getUserByUsername(String username) {
        log.debug("Buscando usuario: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> 
                    new RuntimeException("Usuario no encontrado: " + username)
                );

        return UserResponse.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet())
                )
                .build();
    }
}
