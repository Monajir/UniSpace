package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.model.PendingRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PendingRolesRepo extends JpaRepository<PendingRole, UUID> {
    boolean existsByEmail(String email);
    void deleteAllByEmail(String email);
}

