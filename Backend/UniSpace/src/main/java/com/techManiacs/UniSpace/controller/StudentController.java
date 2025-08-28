package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.model.Routine;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.BookingRepo;
import com.techManiacs.UniSpace.repository.ClassroomRepo;
import com.techManiacs.UniSpace.repository.RoutineRepo;
import com.techManiacs.UniSpace.repository.UserRepository;
import com.techManiacs.UniSpace.service.BookingService;
import com.techManiacs.UniSpace.utils.BookingResponse;
import com.techManiacs.UniSpace.utils.RoutineResponse;
import org.apache.coyote.Response;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Autowired
    UserRepository userRepo;

    @Autowired
    ClassroomRepo classroomRepo;

    @Autowired
    RoutineRepo routineRepo;

    @Autowired
    BookingService bookingService;

    @GetMapping("/auth-check")
    public String authCheck() {
        return "OK";
    }

    @GetMapping("/my/bookings")
    public ResponseEntity<?> getMyBookings() {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            ObjectId userId = userRepo.findByEmail(email).getId();

            List<Booking> result = bookingService.getBookingsOfUserId(userId);

            List<BookingResponse> responses = new ArrayList<>();
            for(Booking booking : result){
                BookingResponse bookingResponse = new BookingResponse();
                bookingResponse.setId(booking.get_id());

                Optional<Classroom> classroom = classroomRepo.findById(booking.getClassroomId());
                classroom.ifPresent(value -> bookingResponse.setRoom(value.getRoom_number()));

                bookingResponse.setDate(booking.getBookingDate());
                String time = booking.getStartTime() + " - " + booking.getEndTime();
                bookingResponse.setTime(time);
                bookingResponse.setStatus(booking.getStatus());
                bookingResponse.setReason(booking.getReason());
                responses.add(bookingResponse);
            }

            return new ResponseEntity<>(responses, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/my/routine")
    public ResponseEntity<?> getMyRoutine() {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userRepo.findByEmail(email);
            String program = user.getProgram();
            Integer semester = user.getSemester();
            Integer section = user.getSection();

            List<Routine> myRoutine = routineRepo.findAllByProgramAndSemesterAndSection(program, semester, section);

            Map<String, List<Routine>> routineByDay = myRoutine.stream()
                    .collect(Collectors.groupingBy(Routine::getDay));

            List<RoutineResponse> response = new ArrayList<>();

            for(Map.Entry<String, List<Routine>>entry : routineByDay.entrySet()){
                String day = entry.getKey();
                List<Routine> dayRoutine = entry.getValue();

                RoutineResponse routineResponse = new RoutineResponse();

                routineResponse.setDay(day);
                List<RoutineResponse.Class> classes = dayRoutine.stream().map(this::convertToClass).collect(Collectors.toList());

                routineResponse.setClasses(classes);
                response.add(routineResponse);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    // Helper function
    private RoutineResponse.Class convertToClass(Routine routine) {
        RoutineResponse.Class classObj = new RoutineResponse.Class();

        String time = routine.getStartTime() + " - " + routine.getEndTime();
        classObj.setTime(time);

        classObj.setSubject(routine.getCourseCode());
        Classroom classroom = classroomRepo.findById(routine.getClassroomId()).orElse(null);
        assert classroom != null;
        classObj.setRoom(classroom.getRoom_number());
        classObj.setType("Regular"); // For now only using regular routine, need to add extra bookings to the Map for setting it dynamic

        return classObj;
    }

    @DeleteMapping("/my/bookings/{bookingId}")
    public ResponseEntity<?> deleteBooking(@PathVariable String bookingId) {
        ObjectId oid = new ObjectId(bookingId);
        boolean response = bookingService.deleteBooking(oid);
        if(response){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
