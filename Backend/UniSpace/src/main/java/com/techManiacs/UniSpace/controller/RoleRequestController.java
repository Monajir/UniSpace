package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.dto.PendingRoleDto;
import com.techManiacs.UniSpace.mapper.ApiMapper;
import com.techManiacs.UniSpace.service.PendingRolesService;
import com.techManiacs.UniSpace.utils.RoleRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/role-requests")
public class RoleRequestController {
    private final PendingRolesService pendingRolesService;
    private final ApiMapper apiMapper;

    public RoleRequestController(PendingRolesService pendingRolesService, ApiMapper apiMapper) {
        this.pendingRolesService = pendingRolesService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public ResponseEntity<List<PendingRoleDto>> getPendingRoleRequests() {
        return ResponseEntity.ok(pendingRolesService.getAllPendingRoles().stream()
                .map(apiMapper::toPendingRoleDto)
                .toList());
    }

    @PostMapping
    public ResponseEntity<Void> createRoleRequest(
            @RequestBody RoleRequest roleRequest,
            Authentication authentication) {
        pendingRolesService.makePendingRole(authentication.getName(), roleRequest.getRequestedRole());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveRoleRequest(@PathVariable UUID id) {
        pendingRolesService.approveRole(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectRoleRequest(@PathVariable UUID id) {
        pendingRolesService.rejectRole(id);
        return ResponseEntity.noContent().build();
    }
}
