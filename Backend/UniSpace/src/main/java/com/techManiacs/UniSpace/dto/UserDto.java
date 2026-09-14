package com.techManiacs.UniSpace.dto;

import com.techManiacs.UniSpace.domain.Role;

import java.util.List;

public record UserDto(
        String id,
        String name,
        String email,
        List<Role> roles,
        String program,
        Integer semester,
        Integer section) {
}
