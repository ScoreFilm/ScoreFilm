package com.camilo.scorefilm.auth.auth.security;

import com.camilo.scorefilm.auth.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {

        var user = userRepository.findByUsernameOrEmail(username)
            
            .orElseThrow(() -> 
                new UsernameNotFoundException(
                    "Usuario o email '" + username + "' no encontrado en la BD"
                )
            );

        return CustomUserDetails.from(user);
    }
}
