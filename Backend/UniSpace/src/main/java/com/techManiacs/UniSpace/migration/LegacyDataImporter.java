package com.techManiacs.UniSpace.migration;

import com.techManiacs.UniSpace.repository.BookingRepo;
import com.techManiacs.UniSpace.repository.ClassroomRepo;
import com.techManiacs.UniSpace.repository.PendingRolesRepo;
import com.techManiacs.UniSpace.repository.NotificationRepo;
import com.techManiacs.UniSpace.repository.ProfileRepo;
import com.techManiacs.UniSpace.repository.RoutineRepo;
import com.techManiacs.UniSpace.repository.UserRepository;
import com.techManiacs.UniSpace.domain.BookingStatus;
import com.techManiacs.UniSpace.service.NotificationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LegacyDataImporter {
    private final UserRepository userRepository;
    private final ProfileRepo profileRepo;
    private final ClassroomRepo classroomRepo;
    private final RoutineRepo routineRepo;
    private final BookingRepo bookingRepo;
    private final PendingRolesRepo pendingRolesRepo;
    private final NotificationRepo notificationRepo;
    private final NotificationService notificationService;
    private final JdbcTemplate jdbcTemplate;

    public LegacyDataImporter(UserRepository userRepository, ProfileRepo profileRepo,
                              ClassroomRepo classroomRepo, RoutineRepo routineRepo,
                              BookingRepo bookingRepo, PendingRolesRepo pendingRolesRepo,
                              NotificationRepo notificationRepo, NotificationService notificationService,
                              JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.profileRepo = profileRepo;
        this.classroomRepo = classroomRepo;
        this.routineRepo = routineRepo;
        this.bookingRepo = bookingRepo;
        this.pendingRolesRepo = pendingRolesRepo;
        this.notificationRepo = notificationRepo;
        this.notificationService = notificationService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void importPlan(MigrationPlan plan) {
        if (plan.hasErrors()) {
            throw new IllegalArgumentException("A migration plan with validation errors cannot be imported");
        }
        if (targetContainsData()) {
            throw new IllegalStateException("PostgreSQL target contains application data; migration requires an empty target");
        }

        plan.users().forEach(user -> {
            jdbcTemplate.update("""
                    INSERT INTO users (id, name, password, email, program, semester, section)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, user.getId(), user.getName(), user.getPassword(), user.getEmail(), user.getProgram(),
                    user.getSemester(), user.getSection());
            user.getRoles().forEach(role -> jdbcTemplate.update(
                    "INSERT INTO user_roles (user_id, role) VALUES (?, ?)", user.getId(), role));
        });

        plan.classrooms().forEach(classroom -> {
            jdbcTemplate.update("""
                    INSERT INTO classrooms (id, room_number, building, capacity, is_available)
                    VALUES (?, ?, ?, ?, ?)
                    """, classroom.getId(), classroom.getRoom_number(), classroom.getBuilding(),
                    classroom.getCapacity(), classroom.getIsAvailable());
            classroom.getEquipment().forEach(equipment -> jdbcTemplate.update(
                    "INSERT INTO classroom_equipment (classroom_id, equipment) VALUES (?, ?)",
                    classroom.getId(), equipment));
        });

        plan.profiles().forEach(profile -> jdbcTemplate.update("""
                INSERT INTO profiles (id, user_id, full_name, email, program, semester, role)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, profile.getId(), profile.getUserId(), profile.getFull_name(), profile.getEmail(),
                profile.getProgram(), profile.getSemester(), profile.getRole()));

        plan.routines().forEach(routine -> jdbcTemplate.update("""
                INSERT INTO routines (id, classroom_id, faculty_name, course_code, day_name,
                                      start_time, end_time, program, semester, section)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, routine.getId(), routine.getClassroomId(), routine.getFacultyName(), routine.getCourseCode(),
                routine.getDay(), routine.getStartTime(), routine.getEndTime(), routine.getProgram(),
                routine.getSemester(), routine.getSection()));

        plan.bookings().forEach(booking -> jdbcTemplate.update("""
                INSERT INTO bookings (id, classroom_id, user_id, faculty_email, faculty_name, reason,
                                      course_code, day_name, booking_date, start_time, end_time, status,
                                      created_at, approved_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, booking.getId(), booking.getClassroomId(), booking.getUserId(), booking.getFacultyEmail(),
                booking.getFacultyName(), booking.getReason(), booking.getCourseCode(), booking.getDay(),
                booking.getBookingDate(), booking.getStartTime(), booking.getEndTime(), booking.getStatus().name(),
                booking.getCreatedAt(), booking.getApprovedAt()));

        plan.pendingRoles().forEach(pendingRole -> jdbcTemplate.update("""
                INSERT INTO pending_roles (id, name, email, role) VALUES (?, ?, ?, ?)
                """, pendingRole.getId(), pendingRole.getName(), pendingRole.getEmail(), pendingRole.getRole()));

        plan.bookings().forEach(booking -> {
            notificationService.bookingRequested(booking);
            if (booking.getStatus() == BookingStatus.BOOKED) {
                notificationService.bookingApproved(booking);
            } else if (booking.getStatus() == BookingStatus.REJECTED) {
                notificationService.bookingRejected(booking);
            }
        });
    }

    private boolean targetContainsData() {
        return userRepository.count() > 0
                || profileRepo.count() > 0
                || classroomRepo.count() > 0
                || routineRepo.count() > 0
                || bookingRepo.count() > 0
                || pendingRolesRepo.count() > 0
                || notificationRepo.count() > 0;
    }
}
