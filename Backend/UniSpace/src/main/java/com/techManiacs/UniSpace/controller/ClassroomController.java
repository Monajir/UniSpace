package com.techManiacs.UniSpace.controller;


import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.dto.ClassroomDto;
import com.techManiacs.UniSpace.mapper.ApiMapper;
import com.techManiacs.UniSpace.service.ClassroomService;
import com.techManiacs.UniSpace.service.BookingService;
import com.techManiacs.UniSpace.utils.RoomScheduleResponse;
import com.techManiacs.UniSpace.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {
    @Autowired
    private ClassroomService classroomService;
    @Autowired
    private ApiMapper apiMapper;
    @Autowired
    private BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<ClassroomDto>> getAllAvailableClassrooms() {
        List<Classroom> result = classroomService.getAllAvailableClassrooms();
        return ResponseEntity.ok(result.stream().map(apiMapper::toClassroomDto).toList());
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<?> getSchedule(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "current") String week) {
        RoomScheduleResponse response = switch (week.toLowerCase()) {
            case "current" -> bookingService.getAllSchedule(id);
            case "next" -> bookingService.getAllScheduleNextWeek(id);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "week must be current or next");
        };
        return ResponseEntity.ok(apiMapper.toRoomScheduleDto(response));
    }
}
