package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.dto.NotificationDto;
import com.techManiacs.UniSpace.mapper.ApiMapper;
import com.techManiacs.UniSpace.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final ApiMapper apiMapper;

    public NotificationController(NotificationService notificationService, ApiMapper apiMapper) {
        this.notificationService = notificationService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getForUser(authentication.getName()).stream()
                .map(apiMapper::toNotificationDto)
                .toList());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable UUID id, Authentication authentication) {
        notificationService.markRead(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        notificationService.markAllRead(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
