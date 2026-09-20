package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.domain.BookingStatus;
import com.techManiacs.UniSpace.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepo extends JpaRepository<Booking, UUID> {
    List<Booking> findAllByClassroomId(UUID classroomId);
    List<Booking> findAllByStatus(BookingStatus status);
    List<Booking> findAllByUserId(UUID userId);
    List<Booking> findAllByFacultyEmailIgnoreCase(String facultyEmail);
    List<Booking> findAllByClassroomIdAndBookingDateAndStatusIn(
            UUID classroomId, LocalDate bookingDate, List<BookingStatus> statuses);
    List<Booking> findAllByClassroomIdAndBookingDateBetweenAndStatusIn(
            UUID classroomId, LocalDate startDate, LocalDate endDate, List<BookingStatus> statuses);
    void deleteAllByUserId(UUID userId);
    void deleteByBookingDateBefore(LocalDate cutoff);
    Optional<Booking> findByIdAndUserId(UUID id, UUID userId);
}
