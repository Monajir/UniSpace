package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.domain.Role;
import com.techManiacs.UniSpace.exception.ApiException;
import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.BookingRepo;
import com.techManiacs.UniSpace.repository.PendingRolesRepo;
import com.techManiacs.UniSpace.repository.ProfileRepo;
import com.techManiacs.UniSpace.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {
    @Mock private UserRepository userRepository;
    @Mock private PendingRolesRepo pendingRolesRepo;
    @Mock private ProfileRepo profileRepo;
    @Mock private BookingRepo bookingRepo;
    @InjectMocks private UserService userService;

    @Test
    void userIsSavedBeforeProfileSoProfileReceivesGeneratedUuid() {
        UUID generatedId = UUID.randomUUID();
        User user = validUser();
        when(userRepository.saveAndFlush(user)).thenAnswer(invocation -> {
            user.setId(generatedId);
            return user;
        });

        User created = userService.createNewUser(user);

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepo).saveAndFlush(profileCaptor.capture());
        assertThat(created.getId()).isEqualTo(generatedId);
        assertThat(profileCaptor.getValue().getUserId()).isEqualTo(generatedId);
        assertThat(profileCaptor.getValue().getRole()).isEqualTo(Role.CR.value());
        assertThat(created.getPassword()).startsWith("$2");
    }

    @Test
    void databaseConstraintFailureReturnsConflict() {
        User user = validUser();
        user.setId(UUID.randomUUID());
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(profileRepo.saveAndFlush(any(Profile.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate profile"));

        assertThatThrownBy(() -> userService.createNewUser(user))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void deceptiveInstitutionalEmailSuffixIsRejected() {
        User user = validUser();
        user.setEmail("demo@example.com@iut-dhaka.edu");

        assertThatThrownBy(() -> userService.createNewUser(user))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void institutionalEmailIsNormalizedBeforePersistence() {
        User user = validUser();
        user.setEmail(" Demo@IUT-DHAKA.EDU ");
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        User created = userService.createNewUser(user);

        assertThat(created.getEmail()).isEqualTo("demo@iut-dhaka.edu");
        verify(userRepository).existsByEmail("demo@iut-dhaka.edu");
    }

    @Test
    void deletingAccountRemovesDependentRecordsBeforeUser() {
        UUID userId = UUID.randomUUID();
        User user = validUser();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(bookingRepo).deleteAllByUserId(userId);
        verify(pendingRolesRepo).deleteAllByEmail(user.getEmail());
        verify(profileRepo).deleteByUserId(userId);
        verify(userRepository).delete(user);
    }

    private User validUser() {
        User user = new User();
        user.setName("Demo CR");
        user.setEmail("demo@iut-dhaka.edu");
        user.setPassword("password123");
        user.setRoles(List.of(Role.STUDENT.value(), Role.CR.value()));
        user.setProgram("CSE");
        user.setSemester(7);
        user.setSection(1);
        return user;
    }
}
