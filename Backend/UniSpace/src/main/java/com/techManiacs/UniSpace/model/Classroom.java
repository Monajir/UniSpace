package com.techManiacs.UniSpace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "classrooms")
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "room_number", nullable = false, unique = true)
    private String room_number;
    @Column(nullable = false)
    private String building;
    @Column(nullable = false)
    private Integer capacity;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "classroom_equipment", joinColumns = @JoinColumn(name = "classroom_id"))
    @Column(name = "equipment")
    private List<String> equipment = new ArrayList<>();
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
}
