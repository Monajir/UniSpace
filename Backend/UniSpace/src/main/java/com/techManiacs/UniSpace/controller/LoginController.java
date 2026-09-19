package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.dto.UserDto;
import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.mapper.ApiMapper;
import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.service.ProfileService;
import com.techManiacs.UniSpace.service.UserDetailsServiceImpl;
import com.techManiacs.UniSpace.service.UserService;
import com.techManiacs.UniSpace.utils.JwtUtil;
import com.techManiacs.UniSpace.utils.LoginRequest;
import com.techManiacs.UniSpace.utils.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final UserService userService;
    private final ProfileService profileService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final ApiMapper apiMapper;

    public LoginController(UserService userService, ProfileService profileService,
            AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService,
            JwtUtil jwtUtil, ApiMapper apiMapper) {
        this.userService = userService;
        this.profileService = profileService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.apiMapper = apiMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), loginRequest.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            String token = jwtUtil.generateToken(userDetails.getUsername());
            UserDto user = apiMapper.toUserDto(userService.getUserByEmail(loginRequest.getEmail()));
            Profile profile = profileService.getProfileByEmail(loginRequest.getEmail());
            return ResponseEntity.ok(new LoginResponse(token, user, apiMapper.toProfileDto(profile)));
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }
    }
}
