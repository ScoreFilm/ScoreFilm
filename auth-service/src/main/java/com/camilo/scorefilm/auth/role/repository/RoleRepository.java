package com.camilo.scorefilm.auth.role.repository;

import com.camilo.scorefilm.auth.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Query("select r from Role r where r.name = :name")
    Optional<Role> findByName(@Param("name") String name);

    @Query("select count(r) > 0 from Role r where r.name = :name")
    boolean existsRoleName(@Param("name") String name);
}
