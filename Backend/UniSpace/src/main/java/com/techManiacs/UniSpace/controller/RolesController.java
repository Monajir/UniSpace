package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.dto.PendingRoleDto;
import com.techManiacs.UniSpace.dto.UserCreateRequest;
import com.techManiacs.UniSpace.dto.UserDto;
import com.techManiacs.UniSpace.mapper.ApiMapper;
import com.techManiacs.UniSpace.repository.UserRepository;
import com.techManiacs.UniSpace.service.PendingRolesService;
import com.techManiacs.UniSpace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/roles")
public class RolesController {

    @Autowired
    private PendingRolesService pendingRolesService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ApiMapper apiMapper;

    @GetMapping("/pending")
    public ResponseEntity<?> getAllPendingRoles() {
        List<PendingRoleDto> responses = pendingRolesService.getAllPendingRoles().stream()
                .map(apiMapper::toPendingRoleDto)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable UUID id) {
        pendingRolesService.approveRole(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable UUID id) {
        pendingRolesService.rejectRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        List<UserDto> result = userRepository.findAll().stream().map(apiMapper::toUserDto).toList();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        User user = userService.deleteUser(id);
        return ResponseEntity.ok("User : " + user.getName() + " deleted successfully!");
    }

    @PostMapping("/create/user")
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {
        User user = apiMapper.toUser(request);
        User created = userService.createNewUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiMapper.toUserDto(created));
    }
}
