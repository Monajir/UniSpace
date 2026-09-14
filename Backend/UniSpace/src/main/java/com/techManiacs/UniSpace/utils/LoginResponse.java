package com.techManiacs.UniSpace.utils;

import com.techManiacs.UniSpace.dto.ProfileDto;
import com.techManiacs.UniSpace.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginResponse {
    private String token;
    private UserDto user;
    private ProfileDto profile;
}
