package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.dto.BookingCreateRequest;
import com.techManiacs.UniSpace.dto.BookingDto;
import com.techManiacs.UniSpace.dto.PendingBookingDto;
import com.techManiacs.UniSpace.mapper.ApiMapper;
import com.techManiacs.UniSpace.service.BookingService;
import com.techManiacs.UniSpace.utils.RoomScheduleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    BookingService bookingService;
    @Autowired
    ApiMapper apiMapper;

//    @GetMapping
//    public ResponseEntity<List<Booking>> getBooking(@RequestParam int classroomId) {
//        List<Booking> bookings = bookingService.getAllBookings();
//        return new ResponseEntity<>(bookings, HttpStatus.OK);
//    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings.stream().map(apiMapper::toBookingDto).toList());
    }
    // Could combine the two and make a single request and response
    @GetMapping("/classSchedule/{id}")
    public ResponseEntity<?> getClassSchedule(@PathVariable UUID id) {
        RoomScheduleResponse response = bookingService.getAllSchedule(id);
        if(response != null) {
            return ResponseEntity.ok(apiMapper.toRoomScheduleDto(response));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/classSchedule/next/{id}")
    public ResponseEntity<?> getClassScheduleNextWeek(@PathVariable UUID id) {
        RoomScheduleResponse response = bookingService.getAllScheduleNextWeek(id);
        if(response != null) {
            return ResponseEntity.ok(apiMapper.toRoomScheduleDto(response));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("room/book")
    public ResponseEntity<BookingDto> addBooking(@RequestBody BookingCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Booking booking = apiMapper.toBooking(request);
        Booking book = bookingService.makeBookingRequest(booking, email);
        return new ResponseEntity<>(apiMapper.toBookingDto(book), HttpStatus.CREATED);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getAllPendingBookings() {
        List<PendingBookingDto> response = bookingService.getAllPendingBookings().stream()
                .map(apiMapper::toPendingBookingDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable UUID id) {
        bookingService.approveBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable UUID id) {
        bookingService.rejectBooking(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/faculty")
    public ResponseEntity<List<PendingBookingDto>> getFacultyBookings(Authentication authentication) {
        List<PendingBookingDto> response = bookingService.getBookingsForFaculty(authentication.getName()).stream()
                .map(apiMapper::toPendingBookingDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/faculty/{id}/approve")
    public ResponseEntity<Void> approveFacultyBooking(
            @PathVariable UUID id,
            Authentication authentication) {
        bookingService.approveBookingForFaculty(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/faculty/{id}/reject")
    public ResponseEntity<Void> rejectFacultyBooking(
            @PathVariable UUID id,
            Authentication authentication) {
        bookingService.rejectBookingForFaculty(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
