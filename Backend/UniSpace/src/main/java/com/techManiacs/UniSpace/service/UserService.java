package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User getUerByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveNewUser(User user) {
        try{
            user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setRoles(Arrays.asList("STUDENT"));
            userRepository.save(user);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public String getUserNameFromId(ObjectId id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(User::getName).orElse(null);
    }
}
