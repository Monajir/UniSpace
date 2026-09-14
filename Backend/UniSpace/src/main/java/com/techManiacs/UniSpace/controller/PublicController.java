package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.UserRepository;
import com.techManiacs.UniSpace.service.ProfileService;
import com.techManiacs.UniSpace.service.UserDetailsServiceImpl;
import com.techManiacs.UniSpace.service.UserService;
import com.techManiacs.UniSpace.utils.JwtUtil;
import com.techManiacs.UniSpace.utils.LoginRequest;
import com.techManiacs.UniSpace.utils.LoginResponse;
import com.techManiacs.UniSpace.utils.SignUpRequest;
import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.dto.UserDto;
import com.techManiacs.UniSpace.mapper.ApiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
@org.springframework.context.annotation.Profile("!migration")
public class PublicController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ApiMapper apiMapper;

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
            UserDto user = apiMapper.toUserDto(userService.getUerByEmail(loginRequest.getEmail()));
            Profile profile = profileService.getProfileByEmail(loginRequest.getEmail());

            LoginResponse response = new LoginResponse(token, user, apiMapper.toProfileDto(profile));
            return new ResponseEntity<>(response, HttpStatus.OK);

        }catch(Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }
    }

//    @PostMapping("/signup")
//    public ResponseEntity<?> createUser(@RequestBody SignUpRequest newUser) {
//
//        User duplicateUser = userRepository.findByEmail(newUser.getEmail());
//        if(duplicateUser != null) {
//            return new ResponseEntity<>("User already exists under the email", HttpStatus.CONFLICT);
//        }
//
//        if(!newUser.getEmail().toLowerCase().endsWith("@iut-dhaka.edu")) {
//            return new ResponseEntity<>("Email address is incorrect", HttpStatus.BAD_REQUEST);
//        }
//
//        boolean isOnlyStudent = userService.saveNewUser(newUser);
//
//        if(!isOnlyStudent) {
//            return new ResponseEntity<>("User request created",HttpStatus.CREATED);
//        }
//        return new ResponseEntity<>("User created successfully",HttpStatus.CREATED);
//    }
}
