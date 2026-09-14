package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { // Actually username = email not changing it now

//        User user = userRepository.findByName(username);

        User user = userRepository.findByEmail(username);

        if(user != null) {
            return org.springframework.security.core.userdetails.User.builder()
//                    .username(user.getName())
                    .username(user.getEmail()) // Switching completely to email
                    .password(user.getPassword())
                    .roles(user.getRoles().toArray(new String[0])) // toArray converts to array of specified type. Here, type is Stirng
                    .build();

        }
        throw new UsernameNotFoundException("user not found with username: "+ username);
    }


}
