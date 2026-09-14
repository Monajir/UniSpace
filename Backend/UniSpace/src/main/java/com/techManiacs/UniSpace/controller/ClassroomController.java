package com.techManiacs.UniSpace.controller;


import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.dto.ClassroomDto;
import com.techManiacs.UniSpace.mapper.ApiMapper;
import com.techManiacs.UniSpace.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {
    @Autowired
    private ClassroomService classroomService;
    @Autowired
    private ApiMapper apiMapper;

    @GetMapping
    public ResponseEntity<List<ClassroomDto>> getAllAvailbleClassrooms(@RequestParam Boolean available) {
        List<Classroom> result = classroomService.getAllAvailableClassrooms();
        return ResponseEntity.ok(result.stream().map(apiMapper::toClassroomDto).toList());
    }
}
