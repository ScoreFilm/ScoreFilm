package com.camilo.scorefilm.auth.user.repository;

import com.camilo.scorefilm.auth.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("select distinct u from User u left join fetch u.roles where u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("select distinct u from User u left join fetch u.roles where u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("""
            select distinct u
            from User u
            left join fetch u.roles
            where u.username = :identifier or u.email = :identifier
            """)
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    @Query("select count(u) > 0 from User u where u.username = :username")
    boolean existsUsername(@Param("username") String username);

    @Query("select count(u) > 0 from User u where u.email = :email")
    boolean existsEmail(@Param("email") String email);
}
