package com.techManiacs.UniSpace.dto;

import com.techManiacs.UniSpace.domain.Role;

public record PendingRoleDto(String id, String email, Role role, String name) {
}
