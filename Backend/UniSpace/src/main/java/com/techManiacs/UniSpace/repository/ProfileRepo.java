package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfileRepo extends JpaRepository<Profile, UUID> {
    Profile findByEmail(String email);
    Profile findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}

