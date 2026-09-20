package com.techManiacs.UniSpace.config;

import com.techManiacs.UniSpace.filter.JwtFilter;
import com.techManiacs.UniSpace.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SpringSecurity {

//    @Autowired
//    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http.cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/actuator/health/**", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/classrooms", "/api/classrooms/*/schedule").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/classrooms").hasRole(Role.ADMIN.value())
                        .requestMatchers(HttpMethod.PATCH, "/api/classrooms/*").hasRole(Role.ADMIN.value())
                        .requestMatchers(HttpMethod.DELETE, "/api/classrooms/*").hasRole(Role.ADMIN.value())
                        .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole(Role.CR.value())
                        .requestMatchers("/api/bookings/assigned-to-me").hasRole(Role.FACULTY.value())
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/bookings/*/approve", "/api/bookings/*/reject")
                                .hasAnyRole(Role.ADMIN.value(), Role.FACULTY.value())
                        .requestMatchers("/api/notifications", "/api/notifications/**").authenticated()
                        .requestMatchers("/api/me/bookings/**", "/api/me/routine").hasRole(Role.STUDENT.value())
                        .requestMatchers(HttpMethod.POST, "/api/role-requests").hasRole(Role.STUDENT.value())
                        .requestMatchers("/api/bookings/**", "/api/role-requests/**", "/api/users/**").hasRole(Role.ADMIN.value())
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
        return auth.getAuthenticationManager();
    }

}
