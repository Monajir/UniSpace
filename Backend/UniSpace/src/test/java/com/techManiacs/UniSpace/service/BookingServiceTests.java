package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.domain.BookingStatus;
import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.model.Routine;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.BookingRepo;
import com.techManiacs.UniSpace.repository.RoutineRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {
    @Mock private BookingRepo bookingRepo;
    @Mock private RoutineRepo routineRepo;
    @Mock private UserService userService;
    @Mock private ClassroomService classroomService;
    @Mock private NotificationService notificationService;
    @InjectMocks private BookingService bookingService;

    @Test
    void ownerCanDeleteBooking() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setUserId(userId);
        when(bookingRepo.findByIdAndUserId(bookingId, userId)).thenReturn(Optional.of(booking));

        bookingService.deleteBookingForUser(bookingId, userId);

        verify(bookingRepo).delete(booking);
    }

    @Test
    void userCannotDeleteBookingTheyDoNotOwn() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(bookingRepo.findByIdAndUserId(bookingId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deleteBookingForUser(bookingId, userId))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(bookingRepo, never()).delete(any());
    }

    @Test
    void validRequestIsCanonicalizedAndSavedAsPending() {
        UUID classroomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(7);
        Booking booking = booking(classroomId, date, LocalTime.of(8, 0), LocalTime.of(9, 15));
        booking.setFacultyEmail(" Faculty@IUT-DHAKA.EDU ");
        User user = new User();
        user.setId(userId);
        when(userService.getUerByEmail("student@iut-dhaka.edu")).thenReturn(user);
        when(classroomService.lockAvailableClassroom(classroomId)).thenReturn(new Classroom());
        when(bookingRepo.save(booking)).thenReturn(booking);

        Booking saved = bookingService.makeBookingRequest(booking, "student@iut-dhaka.edu");

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(saved.getDay()).isEqualToIgnoringCase(date.getDayOfWeek().name());
        assertThat(saved.getFacultyEmail()).isEqualTo("faculty@iut-dhaka.edu");
        verify(notificationService).bookingRequested(saved);
    }

    @Test
    void malformedFacultyEmailIsRejectedBeforeAvailabilityChecks() {
        UUID classroomId = UUID.randomUUID();
        Booking booking = booking(classroomId, LocalDate.now().plusDays(1),
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        booking.setFacultyEmail("faculty@example.com@iut-dhaka.edu");
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getUerByEmail("student@iut-dhaka.edu")).thenReturn(user);

        assertThatThrownBy(() -> bookingService.makeBookingRequest(booking, "student@iut-dhaka.edu"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(classroomService, never()).lockAvailableClassroom(any());
        verify(bookingRepo, never()).save(any());
    }

    @Test
    void requestLongerThanPublishedLimitIsRejected() {
        Booking booking = booking(UUID.randomUUID(), LocalDate.now().plusDays(1),
                LocalTime.of(8, 0), LocalTime.of(9, 16));
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getUerByEmail("student@iut-dhaka.edu")).thenReturn(user);

        assertThatThrownBy(() -> bookingService.makeBookingRequest(booking, "student@iut-dhaka.edu"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(bookingRepo, never()).save(any());
    }

    @Test
    void manuallyEnteredTimeThatIsNotAPublishedSlotIsRejected() {
        Booking booking = booking(UUID.randomUUID(), LocalDate.now().plusDays(1),
                LocalTime.of(8, 10), LocalTime.of(9, 10));
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getUerByEmail("student@iut-dhaka.edu")).thenReturn(user);

        assertThatThrownBy(() -> bookingService.makeBookingRequest(booking, "student@iut-dhaka.edu"))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getMessage()).contains("published schedule slot");
                });
        verify(classroomService, never()).lockAvailableClassroom(any());
        verify(bookingRepo, never()).save(any());
    }

    @Test
    void requestWithoutCourseCodeIsRejected() {
        Booking booking = booking(UUID.randomUUID(), LocalDate.now().plusDays(1),
                LocalTime.of(8, 0), LocalTime.of(9, 15));
        booking.setCourseCode("  ");
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getUerByEmail("student@iut-dhaka.edu")).thenReturn(user);

        assertThatThrownBy(() -> bookingService.makeBookingRequest(booking, "student@iut-dhaka.edu"))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getMessage()).contains("course code is required");
                });
        verify(classroomService, never()).lockAvailableClassroom(any());
        verify(bookingRepo, never()).save(any());
    }

    @Test
    void pastBookingDateIsRejected() {
        Booking booking = booking(UUID.randomUUID(), LocalDate.now().minusDays(1),
                LocalTime.of(8, 0), LocalTime.of(9, 0));
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getUerByEmail("student@iut-dhaka.edu")).thenReturn(user);

        assertThatThrownBy(() -> bookingService.makeBookingRequest(booking, "student@iut-dhaka.edu"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void routineOverlapIsRejected() {
        UUID classroomId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(2);
        Booking booking = booking(classroomId, date, LocalTime.of(10, 30), LocalTime.of(11, 45));
        User user = new User();
        user.setId(UUID.randomUUID());
        Routine routine = new Routine();
        routine.setDay(date.getDayOfWeek().name());
        routine.setStartTime(LocalTime.of(10, 30));
        routine.setEndTime(LocalTime.of(11, 30));
        when(userService.getUerByEmail("student@iut-dhaka.edu")).thenReturn(user);
        when(classroomService.lockAvailableClassroom(classroomId)).thenReturn(new Classroom());
        when(routineRepo.findAllByClassroomId(classroomId)).thenReturn(List.of(routine));

        assertThatThrownBy(() -> bookingService.makeBookingRequest(booking, "student@iut-dhaka.edu"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void roomScheduleExposesPendingAndApprovedBookingsButNotRejectedOnes() {
        UUID classroomId = UUID.randomUUID();
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        Booking approved = booking(classroomId, monday, LocalTime.of(8, 0), LocalTime.of(9, 0));
        approved.setStatus(BookingStatus.BOOKED);
        Booking pending = booking(classroomId, monday, LocalTime.of(9, 0), LocalTime.of(10, 0));
        pending.setStatus(BookingStatus.PENDING);
        Booking rejected = booking(classroomId, monday, LocalTime.of(10, 0), LocalTime.of(11, 0));
        rejected.setStatus(BookingStatus.REJECTED);
        when(bookingRepo.findAllByClassroomId(classroomId)).thenReturn(List.of(approved, pending, rejected));
        when(routineRepo.findAllByClassroomId(classroomId)).thenReturn(List.of());

        assertThat(bookingService.getAllSchedule(classroomId).getExtras())
                .containsExactly(approved, pending);
    }

    @Test
    void nextWeekScheduleExposesPendingBooking() {
        UUID classroomId = UUID.randomUUID();
        LocalDate nextMonday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(1);
        Booking pending = booking(classroomId, nextMonday, LocalTime.of(9, 0), LocalTime.of(10, 0));
        pending.setStatus(BookingStatus.PENDING);
        when(bookingRepo.findAllByClassroomId(classroomId)).thenReturn(List.of(pending));
        when(routineRepo.findAllByClassroomId(classroomId)).thenReturn(List.of());

        assertThat(bookingService.getAllScheduleNextWeek(classroomId).getExtras())
                .containsExactly(pending);
    }

    @Test
    void overlappingPendingBookingTemporarilyReservesSlot() {
        UUID classroomId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(2);
        Booking requested = booking(classroomId, date, LocalTime.of(10, 30), LocalTime.of(11, 45));
        Booking pending = booking(classroomId, date, LocalTime.of(10, 30), LocalTime.of(11, 30));
        pending.setStatus(BookingStatus.PENDING);
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getUerByEmail("student@iut-dhaka.edu")).thenReturn(user);
        when(classroomService.lockAvailableClassroom(classroomId)).thenReturn(new Classroom());
        when(bookingRepo.findAllByClassroomIdAndBookingDateAndStatusIn(
                classroomId, date, List.of(BookingStatus.PENDING, BookingStatus.BOOKED)))
                .thenReturn(List.of(pending));

        assertThatThrownBy(() -> bookingService.makeBookingRequest(requested, "student@iut-dhaka.edu"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(bookingRepo, never()).save(any());
    }

    @Test
    void adjacentPendingBookingDoesNotReserveFollowingSlot() {
        UUID classroomId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(2);
        Booking requested = booking(classroomId, date, LocalTime.of(9, 15), LocalTime.of(10, 30));
        Booking pending = booking(classroomId, date, LocalTime.of(8, 0), LocalTime.of(9, 15));
        pending.setStatus(BookingStatus.PENDING);
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getUerByEmail("student@iut-dhaka.edu")).thenReturn(user);
        when(classroomService.lockAvailableClassroom(classroomId)).thenReturn(new Classroom());
        when(bookingRepo.findAllByClassroomIdAndBookingDateAndStatusIn(
                classroomId, date, List.of(BookingStatus.PENDING, BookingStatus.BOOKED)))
                .thenReturn(List.of(pending));
        when(bookingRepo.save(requested)).thenReturn(requested);

        assertThat(bookingService.makeBookingRequest(requested, "student@iut-dhaka.edu"))
                .isSameAs(requested);
    }

    @Test
    void approvalLocksClassroomAndFlushesApprovedState() {
        UUID bookingId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        Booking booking = booking(classroomId, LocalDate.now().plusDays(3),
                LocalTime.of(14, 30), LocalTime.of(15, 45));
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepo.findById(bookingId)).thenReturn(Optional.of(booking));
        when(classroomService.lockAvailableClassroom(classroomId)).thenReturn(new Classroom());
        when(routineRepo.findAllByClassroomId(classroomId)).thenReturn(List.of());

        bookingService.approveBooking(bookingId);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.BOOKED);
        verify(classroomService).lockAvailableClassroom(classroomId);
        verify(bookingRepo).saveAndFlush(booking);
        verify(notificationService).bookingApproved(booking);
    }

    @Test
    void facultyBookingsAreLoadedFromAuthenticatedEmailWithPendingFirst() {
        Booking rejected = booking(UUID.randomUUID(), LocalDate.now().plusDays(1),
                LocalTime.of(9, 15), LocalTime.of(10, 30));
        rejected.setStatus(BookingStatus.REJECTED);
        Booking pending = booking(UUID.randomUUID(), LocalDate.now().plusDays(2),
                LocalTime.of(10, 30), LocalTime.of(11, 45));
        pending.setStatus(BookingStatus.PENDING);
        when(bookingRepo.findAllByFacultyEmailIgnoreCase("faculty@iut-dhaka.edu"))
                .thenReturn(List.of(rejected, pending));

        assertThat(bookingService.getBookingsForFaculty(" Faculty@IUT-DHAKA.EDU "))
                .extracting(response -> response.getPending().getStatus())
                .containsExactly(BookingStatus.PENDING, BookingStatus.REJECTED);
    }

    @Test
    void assignedFacultyCanApproveBooking() {
        UUID bookingId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        Booking booking = booking(classroomId, LocalDate.now().plusDays(3),
                LocalTime.of(14, 30), LocalTime.of(15, 45));
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepo.findById(bookingId)).thenReturn(Optional.of(booking));
        when(classroomService.lockAvailableClassroom(classroomId)).thenReturn(new Classroom());
        when(routineRepo.findAllByClassroomId(classroomId)).thenReturn(List.of());

        bookingService.approveBookingForFaculty(bookingId, " Faculty@IUT-DHAKA.EDU ");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.BOOKED);
        verify(bookingRepo).saveAndFlush(booking);
    }

    @Test
    void facultyCannotApproveBookingAssignedToSomeoneElse() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = booking(UUID.randomUUID(), LocalDate.now().plusDays(3),
                LocalTime.of(13, 0), LocalTime.of(14, 0));
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.PENDING);
        booking.setFacultyEmail("another.faculty@iut-dhaka.edu");
        when(bookingRepo.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBookingForFaculty(
                bookingId, "faculty@iut-dhaka.edu"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(classroomService, never()).lockAvailableClassroom(any());
        verify(bookingRepo, never()).saveAndFlush(any());
    }

    @Test
    void assignedFacultyCanRejectBooking() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = booking(UUID.randomUUID(), LocalDate.now().plusDays(3),
                LocalTime.of(13, 0), LocalTime.of(14, 0));
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepo.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingService.rejectBookingForFaculty(bookingId, "faculty@iut-dhaka.edu");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepo).save(booking);
        verify(notificationService).bookingRejected(booking);
    }

    private Booking booking(UUID classroomId, LocalDate date, LocalTime start, LocalTime end) {
        Booking booking = new Booking();
        booking.setClassroomId(classroomId);
        booking.setFacultyEmail("faculty@iut-dhaka.edu");
        booking.setCourseCode("CSE 451");
        booking.setReason("Academic session");
        booking.setBookingDate(date);
        booking.setStartTime(start);
        booking.setEndTime(end);
        return booking;
    }
}
