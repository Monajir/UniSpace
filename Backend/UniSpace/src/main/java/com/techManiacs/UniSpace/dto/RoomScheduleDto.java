package com.techManiacs.UniSpace.dto;

import java.util.List;

public record RoomScheduleDto(List<RoutineDto> regular, List<BookingDto> extras) {
}
