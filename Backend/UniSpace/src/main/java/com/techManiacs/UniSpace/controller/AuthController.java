package com.techManiacs.UniSpace.controller;

import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.service.ProfileService;
import com.techManiacs.UniSpace.service.UserService;
import com.techManiacs.UniSpace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private ProfileService profileService;

    @GetMapping("/api/auth/me")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract the token from the Bearer header
            if (!authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Invalid Authorization header");
            }
            String token = authHeader.substring(7); // Remove "Bearer "
            String username = jwtUtil.extractUsername(token);

            if(!jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(401).body("Invalid Token");
            }

            User user =  userService.getUerByEmail(username);
            Profile profile = profileService.getProfileByEmail(username);
            String newToken = null;

            if (jwtUtil.isAboutToExpire(token)) {
                newToken = jwtUtil.generateToken(username);
            }

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("profile", profile);
            response.put("message", "Token is valid");

            if (newToken != null) {
                response.put("token", newToken);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token is invalid or expired");
        }
        }
}
