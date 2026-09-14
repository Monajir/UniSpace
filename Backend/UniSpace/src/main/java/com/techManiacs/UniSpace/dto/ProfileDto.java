package com.techManiacs.UniSpace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techManiacs.UniSpace.domain.Role;

public record ProfileDto(
        String id,
        @JsonProperty("user_id") String userId,
        @JsonProperty("full_name") String fullName,
        String email,
        String program,
        String semester,
        Role role) {
}
