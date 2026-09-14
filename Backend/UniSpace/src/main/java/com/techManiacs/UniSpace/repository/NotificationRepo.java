package com.techManiacs.UniSpace.repository;

import com.techManiacs.UniSpace.domain.NotificationType;
import com.techManiacs.UniSpace.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepo extends JpaRepository<Notification, UUID> {
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByUserIdAndBookingIdAndType(UUID userId, UUID bookingId, NotificationType type);
}
