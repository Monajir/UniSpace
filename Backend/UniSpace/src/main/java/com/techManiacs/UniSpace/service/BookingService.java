package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.model.Routine;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.BookingRepo;
import com.techManiacs.UniSpace.repository.RoutineRepo;
import com.techManiacs.UniSpace.utils.PendingResponse;
import com.techManiacs.UniSpace.utils.RoomScheduleResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class BookingService {

    @Autowired
    BookingRepo bookingRepo;

    @Autowired
    RoutineRepo routineRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private ClassroomService classroomService;

    public List<Booking> getAllBookings(){
        return bookingRepo.findAll();
    }

    // Present Week
    public RoomScheduleResponse getAllSchedule(ObjectId roomId){
        List<Booking> extras = bookingRepo.findAllByClassroomId(roomId);
        List<Routine> regular = routineRepo.findAllByClassroomId(roomId);

        // MONDAY start of week
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate friday = today.with(DayOfWeek.FRIDAY);

        extras = extras.stream()
                .filter(b -> {
                    LocalDate bookingDate = LocalDate.parse(b.getBookingDate()); // "YYYY-MM-DD"
                    return !bookingDate.isBefore(monday) && !bookingDate.isAfter(friday);
                })
                .toList();

        RoomScheduleResponse response = new RoomScheduleResponse();
        response.setExtras(extras);
        response.setRegular(regular);

        return response;
    }

    public RoomScheduleResponse getAllScheduleNextWeek(ObjectId roomId){
        List<Booking> extras = bookingRepo.findAllByClassroomId(roomId);
        List<Routine> regular = null;

        // MONDAY start of week
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate friday = today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));

        extras = extras.stream()
                .filter(b -> {
                    LocalDate bookingDate = LocalDate.parse(b.getBookingDate()); // "YYYY-MM-DD"
                    return !bookingDate.isBefore(monday) && !bookingDate.isAfter(friday);
                })
                .toList();

        RoomScheduleResponse response = new RoomScheduleResponse();
        response.setExtras(extras);
        response.setRegular(regular);

        return response;
    }

    public Booking makeBookingRequest(Booking booking, String email){
        try{
            User user = userService.getUerByEmail(email);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            booking.setCreatedAt(LocalDateTime.now().format(formatter));
            booking.setUserId(user.getId());
            booking.setStatus("pending");
            String faculty_name = booking.getFacultyEmail().split("@")[0];
            booking.setFacultyName(faculty_name);
            bookingRepo.save(booking);

            return booking;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<PendingResponse> getAllPendingBookings(){
//        return bookingRepo.findAllByStatus("pending");
        List<Booking> bookings = bookingRepo.findAllByStatus("pending");
        List<PendingResponse> responses = new ArrayList<>();

        for(Booking booking : bookings){
            PendingResponse response = new PendingResponse();
            response.setPending(booking);
            response.setUser_name(userService.getUserNameFromId(booking.getUserId()));
            response.setRoom_number(classroomService.getClassroomNameById(booking.getClassroomId()));
            responses.add(response);
        }

        return responses;
    }

    public void approveBooking(ObjectId id) {
        Booking booking = bookingRepo.findById(id).orElse(null);
        if(booking == null){
            throw new RuntimeException("Booking not found");
        }
        if(!booking.getStatus().equals("pending")){
            throw new RuntimeException("Only pending bookings can be approved");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        booking.setStatus("booked");
        booking.setApprovedAt(LocalDateTime.now().format(formatter));
        bookingRepo.save(booking);
    }

    public void rejectBooking(ObjectId id) {
        Booking booking = bookingRepo.findById(id).orElse(null);
        if(booking == null){
            throw new RuntimeException("Booking not found");
        }
        if(!booking.getStatus().equals("pending")){
            throw new RuntimeException("Only pending bookings can be rejected");
        }
        bookingRepo.delete(booking);
    }

    public List<Booking> getBookingsOfUserId(ObjectId id){
        return bookingRepo.findAllByUserId(id);
    }

    public boolean deleteBooking(ObjectId id){
        Booking booking = bookingRepo.findById(id).orElse(null);
        if(booking == null){
            throw new RuntimeException("Booking not found");
        }
        bookingRepo.delete(booking);
        return true;
    }
}
