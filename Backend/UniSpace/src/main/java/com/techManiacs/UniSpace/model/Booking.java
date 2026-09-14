package com.techManiacs.UniSpace.model;

import com.techManiacs.UniSpace.domain.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_room_date_status", columnList = "classroom_id, booking_date, status")
})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "classroom_id", nullable = false)
    private UUID classroomId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "faculty_email", nullable = false)
    private String facultyEmail;
    @Column(name = "faculty_name", nullable = false)
    private String facultyName;
    @Column(nullable = false, length = 1000)
    private String reason;
    @Column(name = "course_code")
    private String courseCode;
    @Column(name = "day_name", nullable = false)
    private String day;
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
    @Column(name = "approved_at")
    private LocalDate approvedAt;
}
