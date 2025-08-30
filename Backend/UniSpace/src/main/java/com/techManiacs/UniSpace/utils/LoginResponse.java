package com.techManiacs.UniSpace.utils;

import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginResponse {
    private String token;
    private User user;
    private Profile profile;
}
