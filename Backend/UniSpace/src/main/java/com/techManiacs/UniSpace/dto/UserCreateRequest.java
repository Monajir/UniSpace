package com.techManiacs.UniSpace.dto;

import com.techManiacs.UniSpace.domain.Role;

import java.util.List;

public record UserCreateRequest(
        String name,
        String email,
        String password,
        List<Role> roles,
        String program,
        Integer semester,
        Integer section) {
}
