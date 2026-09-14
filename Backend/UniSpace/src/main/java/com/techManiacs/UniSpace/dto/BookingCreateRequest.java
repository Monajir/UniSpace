package com.techManiacs.UniSpace.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookingCreateRequest(
        @JsonProperty("classroom_id") String classroomId,
        @JsonProperty("faculty_email") String facultyEmail,
        String reason,
        @JsonProperty("course_code") String courseCode,
        String day,
        @JsonProperty("booking_date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate bookingDate,
        @JsonProperty("start_time") @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonProperty("end_time") @JsonFormat(pattern = "HH:mm") LocalTime endTime) {
}
