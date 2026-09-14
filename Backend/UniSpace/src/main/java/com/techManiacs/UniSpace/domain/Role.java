package com.techManiacs.UniSpace.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public enum Role {
    STUDENT,
    CR,
    FACULTY,
    ADMIN;

    @JsonValue
    public String value() {
        return name();
    }

    @JsonCreator
    public static Role fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Role is required");
        }
        return Role.valueOf(value.trim().toUpperCase());
    }

    public static Role primary(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return STUDENT;
        }
        List<Role> parsed = roles.stream().map(Role::fromValue).toList();
        for (Role candidate : List.of(ADMIN, FACULTY, CR, STUDENT)) {
            if (parsed.contains(candidate)) {
                return candidate;
            }
        }
        return STUDENT;
    }
}
