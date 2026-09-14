package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.domain.Role;
import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.model.PendingRole;
import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.PendingRolesRepo;
import com.techManiacs.UniSpace.repository.ProfileRepo;
import com.techManiacs.UniSpace.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingRolesServiceTests {
    @Mock private PendingRolesRepo pendingRolesRepo;
    @Mock private UserRepository userRepo;
    @Mock private ProfileRepo profileRepo;
    @InjectMocks private PendingRolesService pendingRolesService;

    @Test
    void approvingCrRoleKeepsUserAndProfileConsistent() {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PendingRole request = new PendingRole(requestId, "Demo User", "demo@iut-dhaka.edu", Role.CR.value());
        User user = new User();
        user.setId(userId);
        user.setEmail(request.getEmail());
        user.setRoles(List.of(Role.STUDENT.value()));
        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setEmail(request.getEmail());
        profile.setRole(Role.STUDENT.value());
        when(pendingRolesRepo.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepo.findByEmail(request.getEmail())).thenReturn(user);
        when(profileRepo.findByUserId(userId)).thenReturn(profile);

        pendingRolesService.approveRole(requestId);

        assertThat(user.getRoles()).containsExactly(Role.STUDENT.value(), Role.CR.value());
        assertThat(profile.getRole()).isEqualTo(Role.CR.value());
        verify(userRepo).save(user);
        verify(profileRepo).save(profile);
        verify(pendingRolesRepo).delete(request);
    }

    @Test
    void duplicatePendingRoleRequestIsRejected() {
        User user = new User();
        user.setName("Demo User");
        user.setEmail("demo@iut-dhaka.edu");
        user.setRoles(List.of(Role.STUDENT.value()));
        when(userRepo.findByEmail(user.getEmail())).thenReturn(user);
        when(pendingRolesRepo.existsByEmail(user.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> pendingRolesService.makePendingRole(user.getEmail(), Role.CR))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already pending");
    }

    @Test
    void missingProfilePreventsPartialRoleChange() {
        UUID requestId = UUID.randomUUID();
        PendingRole request = new PendingRole(requestId, "Demo User", "demo@iut-dhaka.edu", Role.CR.value());
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setRoles(List.of(Role.STUDENT.value()));
        when(pendingRolesRepo.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepo.findByEmail(request.getEmail())).thenReturn(user);

        assertThatThrownBy(() -> pendingRolesService.approveRole(requestId))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("profile is missing");
        verify(userRepo, never()).save(any());
    }
}

