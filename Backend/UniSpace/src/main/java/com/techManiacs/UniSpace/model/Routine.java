package com.techManiacs.UniSpace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "routines", indexes = @Index(name = "idx_routines_room_day", columnList = "classroom_id, day_name"))
public class Routine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "classroom_id", nullable = false)
    private UUID classroomId;
    @Column(name = "faculty_name", nullable = false)
    private String facultyName;
    @Column(name = "course_code", nullable = false)
    private String courseCode;
    @Column(name = "day_name", nullable = false)
    private String day;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    private String program;
    private Integer semester;
    private Integer section;
}
