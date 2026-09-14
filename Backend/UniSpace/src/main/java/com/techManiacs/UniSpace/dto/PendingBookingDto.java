package com.techManiacs.UniSpace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PendingBookingDto(
        BookingDto pending,
        @JsonProperty("user_name") String userName,
        @JsonProperty("room_number") String roomNumber) {
}
