package com.camilo.scorefilm.auth.role.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_role")
    private UUID idRole;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
}
