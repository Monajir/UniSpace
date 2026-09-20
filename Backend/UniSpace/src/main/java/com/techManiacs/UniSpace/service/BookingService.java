package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.domain.BookingStatus;
import com.techManiacs.UniSpace.domain.Role;
import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.model.Routine;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.BookingRepo;
import com.techManiacs.UniSpace.repository.RoutineRepo;
import com.techManiacs.UniSpace.utils.PendingResponse;
import com.techManiacs.UniSpace.utils.RoomScheduleResponse;
import com.techManiacs.UniSpace.utils.InstitutionalEmailValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingService {

    private static final Set<BookingSlot> BOOKING_SLOTS = Set.of(
            new BookingSlot(LocalTime.of(8, 0), LocalTime.of(9, 15)),
            new BookingSlot(LocalTime.of(9, 15), LocalTime.of(10, 30)),
            new BookingSlot(LocalTime.of(10, 30), LocalTime.of(11, 45)),
            new BookingSlot(LocalTime.of(11, 45), LocalTime.of(13, 0)),
            new BookingSlot(LocalTime.of(14, 30), LocalTime.of(15, 45)),
            new BookingSlot(LocalTime.of(15, 45), LocalTime.of(17, 0)));
    private static final List<BookingStatus> CALENDAR_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.BOOKED);

    private final BookingRepo bookingRepo;
    private final RoutineRepo routineRepo;
    private final UserService userService;
    private final ClassroomService classroomService;
    private final NotificationService notificationService;

    public BookingService(
            BookingRepo bookingRepo,
            RoutineRepo routineRepo,
            UserService userService,
            ClassroomService classroomService,
            NotificationService notificationService) {
        this.bookingRepo = bookingRepo;
        this.routineRepo = routineRepo;
        this.userService = userService;
        this.classroomService = classroomService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    @Transactional(readOnly = true)
    public RoomScheduleResponse getSchedule(UUID roomId, LocalDate requestedWeekStart) {
        LocalDate monday = requestedWeekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate friday = monday.plusDays(4);
        List<Booking> extras = bookingRepo.findAllByClassroomIdAndBookingDateBetweenAndStatusIn(
                roomId, monday, friday, CALENDAR_STATUSES);
        List<Routine> regular = routineRepo.findAllByClassroomId(roomId);

        return new RoomScheduleResponse(regular, extras);
    }

    @Transactional
    public Booking makeBookingRequest(Booking booking, String email) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists");
        }

        validateBookingRequest(booking);
        booking.setFacultyEmail(InstitutionalEmailValidator.normalize(booking.getFacultyEmail()));
        User faculty = requireRegisteredFaculty(booking.getFacultyEmail());
        validateSchedule(booking, true, true);
        booking.setCreatedAt(LocalDate.now());
        booking.setUserId(user.getId());
        booking.setStatus(BookingStatus.PENDING);
        booking.setFacultyName(faculty.getName());
        Booking saved = bookingRepo.save(booking);
        notificationService.bookingRequested(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<PendingResponse> getAllPendingBookings() {
        return pendingResponses(bookingRepo.findAllByStatus(BookingStatus.PENDING));
    }

    @Transactional
    public void approveBooking(UUID id) {
        Booking booking = requirePendingBooking(id);
        approve(booking);
    }

    @Transactional
    public void approveBookingForFaculty(UUID id, String facultyEmail) {
        Booking booking = requireFacultyBooking(id, facultyEmail);
        approve(booking);
    }

    private void approve(Booking booking) {
        classroomService.lockAvailableClassroom(booking.getClassroomId());
        validateSchedule(booking, false, false);
        booking.setStatus(BookingStatus.BOOKED);
        booking.setApprovedAt(LocalDate.now());
        bookingRepo.saveAndFlush(booking);
        notificationService.bookingApproved(booking);
    }

    @Transactional
    public void rejectBooking(UUID id) {
        Booking booking = requirePendingBooking(id);
        reject(booking);
    }

    @Transactional
    public void rejectBookingForFaculty(UUID id, String facultyEmail) {
        Booking booking = requireFacultyBooking(id, facultyEmail);
        reject(booking);
    }

    private void reject(Booking booking) {
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepo.save(booking);
        notificationService.bookingRejected(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsOfUserId(UUID id) {
        return bookingRepo.findAllByUserId(id);
    }

    @Transactional
    public void deleteBookingForUser(UUID id, UUID userId) {
        Booking booking = bookingRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        bookingRepo.delete(booking);
    }

    @Transactional(readOnly = true)
    public List<PendingResponse> getBookingsForFaculty(String email) {
        List<Booking> bookings = bookingRepo
                .findAllByFacultyEmailIgnoreCase(InstitutionalEmailValidator.normalize(email))
                .stream()
                .sorted(Comparator
                        .comparing((Booking booking) -> booking.getStatus() != BookingStatus.PENDING)
                        .thenComparing(Booking::getBookingDate)
                        .thenComparing(Booking::getStartTime))
                .toList();
        return pendingResponses(bookings);
    }

    private List<PendingResponse> pendingResponses(List<Booking> bookings) {
        List<PendingResponse> responses = new ArrayList<>();
        for (Booking booking : bookings) {
            PendingResponse response = new PendingResponse();
            response.setPending(booking);
            response.setUser_name(userService.getUserNameFromId(booking.getUserId()));
            response.setRoom_number(classroomService.getClassroomNameById(booking.getClassroomId()));
            responses.add(response);
        }
        return responses;
    }

    private Booking requirePendingBooking(UUID id) {
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Only pending bookings can be changed");
        }
        return booking;
    }

    private Booking requireFacultyBooking(UUID id, String facultyEmail) {
        Booking booking = requirePendingBooking(id);
        String authenticatedEmail = InstitutionalEmailValidator.normalize(facultyEmail);
        String assignedEmail = InstitutionalEmailValidator.normalize(booking.getFacultyEmail());
        if (!assignedEmail.equals(authenticatedEmail)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Booking not found");
        }
        return booking;
    }

    private void validateBookingRequest(Booking booking) {
        if (booking.getClassroomId() == null || booking.getBookingDate() == null
                || booking.getStartTime() == null || booking.getEndTime() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Classroom, date, start time, and end time are required");
        }
        if (!InstitutionalEmailValidator.isValid(booking.getFacultyEmail())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A valid IUT faculty email is required");
        }
        if (booking.getReason() == null || booking.getReason().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A booking reason is required");
        }
        if (booking.getCourseCode() == null || booking.getCourseCode().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A course code is required");
        }
        String courseCode = booking.getCourseCode().trim();
        if (courseCode.length() > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Course code cannot exceed 100 characters");
        }
        booking.setCourseCode(courseCode);
        if (booking.getBookingDate().isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Booking date cannot be in the past");
        }
        validateTimes(booking.getStartTime(), booking.getEndTime());
        booking.setDay(day(booking.getBookingDate()));
    }

    private User requireRegisteredFaculty(String email) {
        User faculty = userService.getUserByEmail(email);
        boolean hasFacultyRole = faculty != null
                && faculty.getRoles() != null
                && faculty.getRoles().stream().anyMatch(Role.FACULTY.value()::equalsIgnoreCase);
        if (!hasFacultyRole) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Faculty email must belong to a registered faculty account");
        }
        return faculty;
    }

    private void validateSchedule(Booking booking, boolean verifyClassroom, boolean includePendingConflicts) {
        if (verifyClassroom) {
            classroomService.lockAvailableClassroom(booking.getClassroomId());
        }
        if (booking.getBookingDate().isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.CONFLICT, "Past bookings cannot be approved");
        }
        booking.setDay(day(booking.getBookingDate()));
        validateTimes(booking.getStartTime(), booking.getEndTime());

        boolean bookingConflict = bookingRepo
                .findAllByClassroomIdAndBookingDateAndStatusIn(
                        booking.getClassroomId(),
                        booking.getBookingDate(),
                        includePendingConflicts ? CALENDAR_STATUSES : List.of(BookingStatus.BOOKED))
                .stream()
                .filter(existing -> booking.getId() == null || !booking.getId().equals(existing.getId()))
                .anyMatch(existing -> overlaps(
                        booking.getStartTime(), booking.getEndTime(),
                        existing.getStartTime(), existing.getEndTime()));

        boolean routineConflict = routineRepo.findAllByClassroomId(booking.getClassroomId()).stream()
                .filter(routine -> routine.getDay() != null && routine.getDay().equalsIgnoreCase(booking.getDay()))
                .anyMatch(routine -> overlaps(
                        booking.getStartTime(), booking.getEndTime(),
                        routine.getStartTime(), routine.getEndTime()));

        if (bookingConflict || routineConflict) {
            throw new ApiException(HttpStatus.CONFLICT, "The classroom is already occupied during this time");
        }
    }

    private void validateTimes(LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Booking end time must be after start time");
        }
        if (!BOOKING_SLOTS.contains(new BookingSlot(start, end))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Booking time must match a published schedule slot");
        }
    }

    private boolean overlaps(LocalTime start, LocalTime end, LocalTime existingStart, LocalTime existingEnd) {
        return start.isBefore(existingEnd) && existingStart.isBefore(end);
    }

    private String day(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private record BookingSlot(LocalTime start, LocalTime end) {
    }
}
