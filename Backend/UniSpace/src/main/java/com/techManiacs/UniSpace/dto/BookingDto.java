package com.techManiacs.UniSpace.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.techManiacs.UniSpace.domain.BookingStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookingDto(
        String id,
        @JsonProperty("classroom_id") String classroomId,
        @JsonProperty("user_id") String userId,
        @JsonProperty("faculty_email") String facultyEmail,
        @JsonProperty("faculty_name") String facultyName,
        String reason,
        @JsonProperty("course_code") String courseCode,
        String day,
        @JsonProperty("booking_date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate bookingDate,
        @JsonProperty("start_time") @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonProperty("end_time") @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        BookingStatus status,
        @JsonProperty("created_at") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate createdAt,
        @JsonProperty("approved_at") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate approvedAt) {
}
