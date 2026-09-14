package com.techManiacs.UniSpace.config;

import com.techManiacs.UniSpace.domain.BookingStatus;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.BookingRepo;
import com.techManiacs.UniSpace.repository.ClassroomRepo;
import com.techManiacs.UniSpace.repository.PendingRolesRepo;
import com.techManiacs.UniSpace.repository.NotificationRepo;
import com.techManiacs.UniSpace.repository.ProfileRepo;
import com.techManiacs.UniSpace.repository.RoutineRepo;
import com.techManiacs.UniSpace.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "jwt.secret=test-only-secret-that-is-at-least-32-bytes",
        "spring.datasource.url=jdbc:h2:mem:demo-seed;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("demo")
class DemoDataInitializerTests {
    @Autowired
    private DemoDataInitializer initializer;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepo profileRepo;
    @Autowired
    private ClassroomRepo classroomRepo;
    @Autowired
    private RoutineRepo routineRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private PendingRolesRepo pendingRolesRepo;
    @Autowired
    private NotificationRepo notificationRepo;

    @Test
    void seedsConnectedPresentationDataAndRemainsIdempotent() {
        assertThat(userRepository.findAll()).hasSize(5);
        assertThat(profileRepo.findAll()).hasSize(5);
        assertThat(classroomRepo.findAll()).hasSize(5);
        assertThat(routineRepo.findAll()).hasSize(11);
        assertThat(bookingRepo.findAll()).hasSize(5);
        assertThat(pendingRolesRepo.findAll()).hasSize(1);
        assertThat(notificationRepo.findAll()).hasSize(11);

        assertThat(userRepository.findByEmail(DemoDataInitializer.STUDENT_EMAIL))
                .extracting(User::getProgram, User::getSemester, User::getSection)
                .containsExactly("CSE", 5, 1);
        assertThat(routineRepo.findAllByProgramAndSemesterAndSection("CSE", 5, 1)).hasSize(8);
        assertThat(bookingRepo.findAllByStatus(BookingStatus.BOOKED)).hasSize(2);
        assertThat(bookingRepo.findAllByStatus(BookingStatus.PENDING)).hasSize(2);
        assertThat(bookingRepo.findAllByStatus(BookingStatus.REJECTED)).hasSize(1);

        assertThat(routineRepo.findAll()).allSatisfy(routine ->
                assertThat(classroomRepo.existsById(routine.getClassroomId())).isTrue());
        assertThat(bookingRepo.findAll()).allSatisfy(booking -> {
            assertThat(classroomRepo.existsById(booking.getClassroomId())).isTrue();
            assertThat(userRepository.existsById(booking.getUserId())).isTrue();
        });
        assertThat(profileRepo.findAll()).allSatisfy(profile ->
                assertThat(userRepository.existsById(profile.getUserId())).isTrue());

        long userCount = userRepository.count();
        long classroomCount = classroomRepo.count();
        long routineCount = routineRepo.count();
        long bookingCount = bookingRepo.count();
        long notificationCount = notificationRepo.count();
        initializer.run(null);

        assertThat(userRepository.count()).isEqualTo(userCount);
        assertThat(classroomRepo.count()).isEqualTo(classroomCount);
        assertThat(routineRepo.count()).isEqualTo(routineCount);
        assertThat(bookingRepo.count()).isEqualTo(bookingCount);
        assertThat(notificationRepo.count()).isEqualTo(notificationCount);
    }
}
