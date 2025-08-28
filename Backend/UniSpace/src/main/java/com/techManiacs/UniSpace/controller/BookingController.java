package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.service.BookingService;
import com.techManiacs.UniSpace.utils.PendingResponse;
import com.techManiacs.UniSpace.utils.RoomScheduleResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    BookingService bookingService;

//    @GetMapping
//    public ResponseEntity<List<Booking>> getBooking(@RequestParam int classroomId) {
//        List<Booking> bookings = bookingService.getAllBookings();
//        return new ResponseEntity<>(bookings, HttpStatus.OK);
//    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }
    // Could combine the two and make a single request and response
    @GetMapping("/classSchedule/{id}")
    public ResponseEntity<?> getClassSchedule(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        RoomScheduleResponse response = bookingService.getAllSchedule(oid);
        if(response != null) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/classSchedule/next/{id}")
    public ResponseEntity<?> getClassScheduleNextWeek(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        RoomScheduleResponse response = bookingService.getAllScheduleNextWeek(oid);
        if(response != null) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("room/book")
    public ResponseEntity<?> addBooking(@RequestBody Booking booking) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Booking book = bookingService.makeBookingRequest(booking, email);
            return new ResponseEntity<>(book, HttpStatus.CREATED);
        }catch(Exception e){
            return null;
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getAllPendingBookings() {
        List<PendingResponse> response = bookingService.getAllPendingBookings();
        if(response != null) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable ObjectId id) {
        try{
            bookingService.approveBooking(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable ObjectId id) {
        try{
            bookingService.rejectBooking(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
