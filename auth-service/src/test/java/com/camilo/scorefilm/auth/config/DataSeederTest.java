package com.camilo.scorefilm.auth.config;

import com.camilo.scorefilm.auth.role.entity.Role;
import com.camilo.scorefilm.auth.role.repository.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataSeederTest {
    @Test
    void run_missingRoles_persistsAdminAndUser() {
        RoleRepository repository = mock(RoleRepository.class);
        when(repository.findByName(any())).thenReturn(Optional.empty());

        new DataSeeder(repository).run();

        verify(repository, times(2)).save(any(Role.class));
    }

    @Test
    void run_existingRoles_doesNotPersistDuplicates() {
        RoleRepository repository = mock(RoleRepository.class);
        when(repository.findByName(any())).thenReturn(Optional.of(Role.builder().name("existing").build()));

        new DataSeeder(repository).run("test");

        verify(repository, never()).save(any(Role.class));
    }
}
