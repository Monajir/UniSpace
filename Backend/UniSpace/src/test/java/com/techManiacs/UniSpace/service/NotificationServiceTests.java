package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.domain.NotificationType;
import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.model.Notification;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.NotificationRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {
    @Mock private NotificationRepo notificationRepo;
    @Mock private UserService userService;
    @Mock private ClassroomService classroomService;
    @InjectMocks private NotificationService notificationService;

    @Test
    void notificationsAreLoadedOnlyForAuthenticatedUser() {
        User user = user();
        Notification notification = new Notification();
        when(userService.getUerByEmail(user.getEmail())).thenReturn(user);
        when(notificationRepo.findAllByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(notification));

        assertThat(notificationService.getForUser(user.getEmail())).containsExactly(notification);
    }

    @Test
    void ownerCanMarkNotificationRead() {
        User user = user();
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notificationId);
        when(userService.getUerByEmail(user.getEmail())).thenReturn(user);
        when(notificationRepo.findByIdAndUserId(notificationId, user.getId()))
                .thenReturn(Optional.of(notification));

        notificationService.markRead(notificationId, user.getEmail());

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepo).save(notification);
    }

    @Test
    void userCannotMarkAnotherUsersNotificationRead() {
        User user = user();
        UUID notificationId = UUID.randomUUID();
        when(userService.getUerByEmail(user.getEmail())).thenReturn(user);
        when(notificationRepo.findByIdAndUserId(notificationId, user.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markRead(notificationId, user.getEmail()))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(notificationRepo, never()).save(any());
    }

    @Test
    void bookingRequestNotifiesRequesterAndAssignedFaculty() {
        Booking booking = booking();
        User faculty = user();
        faculty.setId(UUID.randomUUID());
        faculty.setEmail(booking.getFacultyEmail());
        faculty.setRoles(List.of("FACULTY"));
        when(classroomService.getClassroomNameById(booking.getClassroomId())).thenReturn("AB-101");
        when(userService.getUerByEmail(booking.getFacultyEmail())).thenReturn(faculty);

        notificationService.bookingRequested(booking);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Notification::getType)
                .containsExactly(NotificationType.BOOKING_REQUESTED, NotificationType.BOOKING_ASSIGNED);
        assertThat(captor.getAllValues())
                .extracting(Notification::getUserId)
                .containsExactly(booking.getUserId(), faculty.getId());
    }

    @Test
    void duplicateBookingEventIsNotCreated() {
        Booking booking = booking();
        when(classroomService.getClassroomNameById(booking.getClassroomId())).thenReturn("AB-101");
        when(notificationRepo.existsByUserIdAndBookingIdAndType(
                booking.getUserId(), booking.getId(), NotificationType.BOOKING_APPROVED))
                .thenReturn(true);

        notificationService.bookingApproved(booking);

        verify(notificationRepo, never()).save(any());
    }

    private User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("student@iut-dhaka.edu");
        return user;
    }

    private Booking booking() {
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setUserId(UUID.randomUUID());
        booking.setClassroomId(UUID.randomUUID());
        booking.setFacultyEmail("faculty@iut-dhaka.edu");
        booking.setCourseCode("CSE 451");
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setStartTime(LocalTime.of(9, 15));
        booking.setEndTime(LocalTime.of(10, 30));
        return booking;
    }
}
