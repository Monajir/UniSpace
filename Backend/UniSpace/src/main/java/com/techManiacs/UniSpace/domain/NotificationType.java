package com.techManiacs.UniSpace.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationType {
    BOOKING_REQUESTED("booking_requested"),
    BOOKING_ASSIGNED("booking_assigned"),
    BOOKING_APPROVED("booking_approved"),
    BOOKING_REJECTED("booking_rejected");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
