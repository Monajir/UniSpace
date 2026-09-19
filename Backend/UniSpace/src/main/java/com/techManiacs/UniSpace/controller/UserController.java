package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.dto.UserCreateRequest;
import com.techManiacs.UniSpace.dto.UserDto;
import com.techManiacs.UniSpace.mapper.ApiMapper;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.UserRepository;
import com.techManiacs.UniSpace.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final ApiMapper apiMapper;

    public UserController(UserRepository userRepository, UserService userService, ApiMapper apiMapper) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(apiMapper::toUserDto).toList());
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreateRequest request) {
        User created = userService.createNewUser(apiMapper.toUser(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(apiMapper.toUserDto(created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
