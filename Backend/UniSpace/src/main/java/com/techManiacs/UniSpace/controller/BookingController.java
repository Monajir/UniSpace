package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.dto.BookingCreateRequest;
import com.techManiacs.UniSpace.dto.BookingDto;
import com.techManiacs.UniSpace.dto.PendingBookingDto;
import com.techManiacs.UniSpace.mapper.ApiMapper;
import com.techManiacs.UniSpace.service.BookingService;
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

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings.stream().map(apiMapper::toBookingDto).toList());
    }
    @PostMapping
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
    public ResponseEntity<Void> approveBooking(@PathVariable UUID id, Authentication authentication) {
        if (hasRole(authentication, "ROLE_FACULTY")) {
            bookingService.approveBookingForFaculty(id, authentication.getName());
        } else {
            bookingService.approveBooking(id);
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectBooking(@PathVariable UUID id, Authentication authentication) {
        if (hasRole(authentication, "ROLE_FACULTY")) {
            bookingService.rejectBookingForFaculty(id, authentication.getName());
        } else {
            bookingService.rejectBooking(id);
        }
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/assigned-to-me")
    public ResponseEntity<List<PendingBookingDto>> getFacultyBookings(Authentication authentication) {
        List<PendingBookingDto> response = bookingService.getBookingsForFaculty(authentication.getName()).stream()
                .map(apiMapper::toPendingBookingDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
