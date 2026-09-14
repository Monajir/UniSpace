package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.domain.Role;
import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.model.PendingRole;
import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.PendingRolesRepo;
import com.techManiacs.UniSpace.repository.ProfileRepo;
import com.techManiacs.UniSpace.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PendingRolesService {
    private final PendingRolesRepo pendingRolesRepo;
    private final UserRepository userRepo;
    private final ProfileRepo profileRepo;

    public PendingRolesService(
            PendingRolesRepo pendingRolesRepo,
            UserRepository userRepo,
            ProfileRepo profileRepo) {
        this.pendingRolesRepo = pendingRolesRepo;
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
    }

    @Transactional(readOnly = true)
    public List<PendingRole> getAllPendingRoles() {
        return pendingRolesRepo.findAll();
    }

    @Transactional
    public void approveRole(UUID id) {
        PendingRole request = pendingRolesRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role request not found"));
        User user = userRepo.findByEmail(request.getEmail());
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Requesting user no longer exists");
        }

        List<String> newRoles = transitionRoles(user.getRoles() == null ? List.of() : user.getRoles(),
                Role.fromValue(request.getRole()));
        Profile profile = profileRepo.findByUserId(user.getId());
        if (profile == null) {
            throw new ApiException(HttpStatus.CONFLICT, "User profile is missing");
        }

        user.setRoles(newRoles);
        profile.setRole(Role.primary(newRoles).value());
        userRepo.save(user);
        profileRepo.save(profile);
        pendingRolesRepo.delete(request);
    }

    @Transactional
    public void rejectRole(UUID id) {
        PendingRole request = pendingRolesRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role request not found"));
        pendingRolesRepo.delete(request);
    }

    @Transactional
    public void makePendingRole(String email, Role role) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (role != Role.CR && role != Role.STUDENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only CR and STUDENT role changes can be requested");
        }

        List<String> roles = user.getRoles() == null ? List.of() : user.getRoles();
        if (role == Role.CR && roles.stream().anyMatch(value -> value.equalsIgnoreCase(Role.CR.value()))) {
            return;
        }
        if (role == Role.STUDENT && roles.stream().noneMatch(value -> value.equalsIgnoreCase(Role.CR.value()))) {
            return;
        }
        if (pendingRolesRepo.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "A role request is already pending");
        }

        PendingRole request = new PendingRole();
        request.setEmail(email);
        request.setName(user.getName());
        request.setRole(role.value());
        pendingRolesRepo.save(request);
    }

    private List<String> transitionRoles(List<String> currentRoles, Role requestedRole) {
        List<String> roles = new ArrayList<>(currentRoles.stream()
                .map(Role::fromValue)
                .map(Role::value)
                .distinct()
                .toList());
        if (requestedRole == Role.CR) {
            if (!roles.contains(Role.STUDENT.value())) {
                roles.add(Role.STUDENT.value());
            }
            if (!roles.contains(Role.CR.value())) {
                roles.add(Role.CR.value());
            }
        } else if (requestedRole == Role.STUDENT) {
            roles.remove(Role.CR.value());
            if (!roles.contains(Role.STUDENT.value())) {
                roles.add(Role.STUDENT.value());
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported role transition");
        }
        return roles;
    }
}

