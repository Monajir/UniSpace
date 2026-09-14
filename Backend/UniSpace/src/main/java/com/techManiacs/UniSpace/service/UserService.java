package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.domain.Role;
import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.BookingRepo;
import com.techManiacs.UniSpace.repository.PendingRolesRepo;
import com.techManiacs.UniSpace.repository.ProfileRepo;
import com.techManiacs.UniSpace.repository.UserRepository;
import com.techManiacs.UniSpace.utils.InstitutionalEmailValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserRepository userRepository;
    private final PendingRolesRepo pendingRolesRepo;
    private final ProfileRepo profileRepo;
    private final BookingRepo bookingRepo;

    public UserService(
            UserRepository userRepository,
            PendingRolesRepo pendingRolesRepo,
            ProfileRepo profileRepo,
            BookingRepo bookingRepo) {
        this.userRepository = userRepository;
        this.pendingRolesRepo = pendingRolesRepo;
        this.profileRepo = profileRepo;
        this.bookingRepo = bookingRepo;
    }

    @Transactional(readOnly = true)
    public User getUerByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User createNewUser(User user) {
        validateNewUser(user);
        user.setEmail(InstitutionalEmailValidator.normalize(user.getEmail()));
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "User already exists under this email");
        }

        List<String> roles = user.getRoles() == null || user.getRoles().isEmpty()
                ? List.of(Role.STUDENT.value())
                : user.getRoles().stream().map(Role::fromValue).distinct().map(Role::value).toList();
        user.setRoles(new ArrayList<>(roles));
        user.setPassword(PASSWORD_ENCODER.encode(user.getPassword()));

        try {
            User savedUser = userRepository.saveAndFlush(user);
            profileRepo.saveAndFlush(profileFor(savedUser));
            return savedUser;
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "User or profile already exists");
        }
    }

    @Transactional(readOnly = true)
    public String getUserNameFromId(UUID id) {
        return userRepository.findById(id).map(User::getName).orElse(null);
    }

    @Transactional
    public User deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        bookingRepo.deleteAllByUserId(id);
        pendingRolesRepo.deleteAllByEmail(user.getEmail());
        profileRepo.deleteByUserId(id);
        userRepository.delete(user);
        return user;
    }

    private void validateNewUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User name is required");
        }
        if (!InstitutionalEmailValidator.isValid(user.getEmail())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The email must use the IUT domain");
        }
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must contain at least 8 characters");
        }
    }

    private Profile profileFor(User user) {
        Profile profile = new Profile();
        profile.setUserId(user.getId());
        profile.setRole(Role.primary(user.getRoles()).value());
        profile.setProgram(user.getProgram());
        profile.setEmail(user.getEmail());
        profile.setFull_name(user.getName());
        if (user.getSemester() != null) {
            profile.setSemester(user.getSemester().toString());
        }
        return profile;
    }
}
