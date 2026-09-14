package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByName(String name);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRolesContaining(String role);
}
