package com.techManiacs.UniSpace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techManiacs.UniSpace.domain.NotificationType;

import java.time.Instant;

public record NotificationDto(
        String id,
        @JsonProperty("booking_id") String bookingId,
        NotificationType type,
        String title,
        String message,
        @JsonProperty("is_read") boolean read,
        @JsonProperty("created_at") Instant createdAt) {
}
