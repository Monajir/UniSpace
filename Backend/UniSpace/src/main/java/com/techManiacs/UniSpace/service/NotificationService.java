package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.domain.NotificationType;
import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.model.Notification;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.NotificationRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepo notificationRepo;
    private final UserService userService;
    private final ClassroomService classroomService;

    public NotificationService(
            NotificationRepo notificationRepo,
            UserService userService,
            ClassroomService classroomService) {
        this.notificationRepo = notificationRepo;
        this.userService = userService;
        this.classroomService = classroomService;
    }

    @Transactional(readOnly = true)
    public List<Notification> getForUser(String email) {
        return notificationRepo.findAllByUserIdOrderByCreatedAtDesc(requireUser(email).getId());
    }

    @Transactional
    public void markRead(UUID id, String email) {
        User user = requireUser(email);
        Notification notification = notificationRepo.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepo.save(notification);
        }
    }

    @Transactional
    public void markAllRead(String email) {
        List<Notification> unread = notificationRepo
                .findAllByUserIdOrderByCreatedAtDesc(requireUser(email).getId())
                .stream()
                .filter(notification -> !notification.isRead())
                .toList();
        unread.forEach(notification -> notification.setRead(true));
        notificationRepo.saveAll(unread);
    }

    @Transactional
    public void bookingRequested(Booking booking) {
        String room = classroomService.getClassroomNameById(booking.getClassroomId());
        create(
                booking.getUserId(),
                booking,
                NotificationType.BOOKING_REQUESTED,
                "Booking request submitted",
                bookingMessage(booking, room, "is awaiting review"));

        User faculty = userService.getUerByEmail(booking.getFacultyEmail());
        if (faculty != null && faculty.getRoles().stream().anyMatch(role -> role.equalsIgnoreCase("FACULTY"))) {
            create(
                    faculty.getId(),
                    booking,
                    NotificationType.BOOKING_ASSIGNED,
                    "New booking request",
                    bookingMessage(booking, room, "needs your review"));
        }
    }

    @Transactional
    public void bookingApproved(Booking booking) {
        create(
                booking.getUserId(),
                booking,
                NotificationType.BOOKING_APPROVED,
                "Booking approved",
                bookingMessage(booking, classroomService.getClassroomNameById(booking.getClassroomId()),
                        "was approved"));
    }

    @Transactional
    public void bookingRejected(Booking booking) {
        create(
                booking.getUserId(),
                booking,
                NotificationType.BOOKING_REJECTED,
                "Booking rejected",
                bookingMessage(booking, classroomService.getClassroomNameById(booking.getClassroomId()),
                        "was rejected"));
    }

    private void create(
            UUID recipientId,
            Booking booking,
            NotificationType type,
            String title,
            String message) {
        if (notificationRepo.existsByUserIdAndBookingIdAndType(recipientId, booking.getId(), type)) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(recipientId);
        notification.setBookingId(booking.getId());
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());
        notificationRepo.save(notification);
    }

    private User requireUser(String email) {
        User user = userService.getUerByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists");
        }
        return user;
    }

    private String bookingMessage(Booking booking, String room, String action) {
        String course = booking.getCourseCode() == null || booking.getCourseCode().isBlank()
                ? "Your classroom request"
                : booking.getCourseCode();
        return "%s for Room %s on %s from %s to %s %s."
                .formatted(course, room, booking.getBookingDate(), booking.getStartTime(), booking.getEndTime(), action);
    }
}
