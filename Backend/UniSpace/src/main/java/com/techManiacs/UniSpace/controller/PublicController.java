package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.service.ProfileService;
import com.techManiacs.UniSpace.service.UserDetailsServiceImpl;
import com.techManiacs.UniSpace.service.UserService;
import com.techManiacs.UniSpace.utils.JwtUtil;
import com.techManiacs.UniSpace.utils.LoginRequest;
import com.techManiacs.UniSpace.utils.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/health-check")
    public String healthCheck() {
        return "OK";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            String token = jwtUtil.generateToken(userDetails.getUsername());

            // Preparing the response
            User user = userService.getUerByEmail(loginRequest.getEmail());
            Profile profile = profileService.getProfileByEmail(loginRequest.getEmail());

            LoginResponse response = new LoginResponse(token, user, profile);
            return new ResponseEntity<>(response, HttpStatus.OK);

        }catch(Exception e) {
//            log.error("Exception occured while creating Authentication Token ", e);
            return new ResponseEntity<>("Incorrect username or password",HttpStatus.BAD_REQUEST);
        }
    }

//    @PostMapping("/signup")
//    public ResponseEntity<?> createUser(@RequestBody User newUser) {
//        userService.saveNewUser(newUser);
//        return new ResponseEntity<>("User created successfully",HttpStatus.CREATED);
//    }
}
