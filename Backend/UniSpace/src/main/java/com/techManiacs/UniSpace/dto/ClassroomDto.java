package com.techManiacs.UniSpace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClassroomDto(
        String id,
        @JsonProperty("room_number") String roomNumber,
        String building,
        Integer capacity,
        List<String> equipment,
        @JsonProperty("is_available") Boolean available) {
}
