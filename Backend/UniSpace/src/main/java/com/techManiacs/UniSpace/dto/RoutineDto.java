package com.techManiacs.UniSpace.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;

public record RoutineDto(
        String id,
        @JsonProperty("classroom_id") String classroomId,
        @JsonProperty("faculty_name") String facultyName,
        @JsonProperty("course_code") String courseCode,
        String day,
        @JsonProperty("start_time") @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonProperty("end_time") @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        String program,
        Integer semester,
        Integer section) {
}
