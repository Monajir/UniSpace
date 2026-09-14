package com.techManiacs.UniSpace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;
    @Column(name = "full_name", nullable = false)
    private String full_name;
    @Column(nullable = false, unique = true)
    private String email;
    private String program;
    private String semester;
    @Column(nullable = false)
    private String role;
}
