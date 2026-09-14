package com.techManiacs.UniSpace.config;

import com.techManiacs.UniSpace.domain.BookingStatus;
import com.techManiacs.UniSpace.domain.Role;
import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.model.PendingRole;
import com.techManiacs.UniSpace.model.Routine;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.repository.BookingRepo;
import com.techManiacs.UniSpace.repository.ClassroomRepo;
import com.techManiacs.UniSpace.repository.PendingRolesRepo;
import com.techManiacs.UniSpace.repository.RoutineRepo;
import com.techManiacs.UniSpace.repository.UserRepository;
import com.techManiacs.UniSpace.service.UserService;
import com.techManiacs.UniSpace.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@Profile("demo")
public class DemoDataInitializer implements ApplicationRunner {
    public static final String STUDENT_EMAIL = "student@iut-dhaka.edu";
    public static final String CR_EMAIL = "cr@iut-dhaka.edu";
    public static final String FACULTY_EMAIL = "faculty@iut-dhaka.edu";
    public static final String CANDIDATE_EMAIL = "candidate@iut-dhaka.edu";
    public static final String STUDENT_PASSWORD = "student12345";
    public static final String CR_PASSWORD = "cr123456";
    public static final String FACULTY_PASSWORD = "faculty12345";
    public static final String CANDIDATE_PASSWORD = "candidate12345";

    private final UserRepository userRepository;
    private final UserService userService;
    private final ClassroomRepo classroomRepo;
    private final RoutineRepo routineRepo;
    private final BookingRepo bookingRepo;
    private final PendingRolesRepo pendingRolesRepo;
    private final NotificationService notificationService;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public DemoDataInitializer(
            UserRepository userRepository,
            UserService userService,
            ClassroomRepo classroomRepo,
            RoutineRepo routineRepo,
            BookingRepo bookingRepo,
            PendingRolesRepo pendingRolesRepo,
            NotificationService notificationService,
            @Value("${app.demo-admin.email}") String adminEmail,
            @Value("${app.demo-admin.password}") String adminPassword,
            @Value("${app.demo-admin.name}") String adminName) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.classroomRepo = classroomRepo;
        this.routineRepo = routineRepo;
        this.bookingRepo = bookingRepo;
        this.pendingRolesRepo = pendingRolesRepo;
        this.notificationService = notificationService;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureUser(adminName, adminEmail, adminPassword, List.of(Role.ADMIN), null, null, null);
        User student = ensureUser("Amina Rahman", STUDENT_EMAIL, STUDENT_PASSWORD,
                List.of(Role.STUDENT), "CSE", 5, 1);
        User cr = ensureUser("Nafis Ahmed", CR_EMAIL, CR_PASSWORD,
                List.of(Role.STUDENT, Role.CR), "CSE", 5, 1);
        ensureUser("Dr. Farhana Karim", FACULTY_EMAIL, FACULTY_PASSWORD,
                List.of(Role.FACULTY), null, null, null);
        User candidate = ensureUser("Samiul Islam", CANDIDATE_EMAIL, CANDIDATE_PASSWORD,
                List.of(Role.STUDENT), "EEE", 3, 2);

        Classroom ab101 = ensureClassroom("AB-101", "Academic Building", 40,
                List.of("Projector", "Whiteboard", "Air conditioning"));
        Classroom ab204 = ensureClassroom("AB-204", "Academic Building", 60,
                List.of("Smart board", "Video conferencing", "Microphone"));
        Classroom cseLab = ensureClassroom("CSE-LAB-2", "Laboratory Building", 35,
                List.of("35 workstations", "Projector", "Development software"));
        Classroom library = ensureClassroom("LIB-301", "Library Building", 25,
                List.of("Interactive display", "Recording system"));
        Classroom auditorium = ensureClassroom("AUDITORIUM", "Administrative Building", 180,
                List.of("Stage", "Sound system", "Dual projectors"));

        ensureRoutine(ab101, "Dr. Farhana Karim", "CSE 451", DayOfWeek.MONDAY,
                "08:00", "09:15", "CSE", 5, 1);
        ensureRoutine(cseLab, "Dr. Farhana Karim", "CSE 453", DayOfWeek.MONDAY,
                "09:15", "10:30", "CSE", 5, 1);
        ensureRoutine(ab204, "Prof. Mahmud Hasan", "CSE 455", DayOfWeek.TUESDAY,
                "08:00", "09:15", "CSE", 5, 1);
        ensureRoutine(library, "Dr. Nusrat Jahan", "HUM 451", DayOfWeek.TUESDAY,
                "10:30", "11:45", "CSE", 5, 1);
        ensureRoutine(ab101, "Dr. Farhana Karim", "CSE 457", DayOfWeek.WEDNESDAY,
                "09:15", "10:30", "CSE", 5, 1);
        ensureRoutine(cseLab, "Prof. Mahmud Hasan", "CSE 459", DayOfWeek.WEDNESDAY,
                "11:45", "13:00", "CSE", 5, 1);
        ensureRoutine(ab204, "Dr. Nusrat Jahan", "CSE 461", DayOfWeek.THURSDAY,
                "10:30", "11:45", "CSE", 5, 1);
        ensureRoutine(auditorium, "Dr. Farhana Karim", "CSE Seminar", DayOfWeek.FRIDAY,
                "08:00", "09:15", "CSE", 5, 1);

        ensureRoutine(ab101, "Dr. Rezaul Haque", "EEE 231", DayOfWeek.TUESDAY,
                "09:15", "10:30", "EEE", 3, 2);
        ensureRoutine(ab204, "Dr. Rezaul Haque", "EEE 233", DayOfWeek.WEDNESDAY,
                "08:00", "09:15", "EEE", 3, 2);
        ensureRoutine(cseLab, "Prof. Sabrina Noor", "EEE 235 Lab", DayOfWeek.THURSDAY,
                "14:30", "15:45", "EEE", 3, 2);

        LocalDate nextMonday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(1);
        ensureBooking(cr, ab101, FACULTY_EMAIL, "Dr. Farhana Karim",
                "Approved capstone consultation", "CSE 499", nextMonday,
                "14:30", "15:45", BookingStatus.BOOKED);
        ensureBooking(cr, ab204, "mahmud@iut-dhaka.edu", "Prof. Mahmud Hasan",
                "Pending make-up class", "CSE 455", nextMonday.plusDays(1),
                "15:45", "17:00", BookingStatus.PENDING);
        ensureBooking(cr, cseLab, FACULTY_EMAIL, "Dr. Farhana Karim",
                "Rejected programming workshop", "CSE 453", nextMonday.plusDays(2),
                "14:30", "15:45", BookingStatus.REJECTED);
        ensureBooking(cr, library, "nusrat@iut-dhaka.edu", "Dr. Nusrat Jahan",
                "Approved thesis presentation practice", "CSE 499", nextMonday.plusDays(4),
                "11:45", "13:00", BookingStatus.BOOKED);
        ensureBooking(cr, auditorium, FACULTY_EMAIL, "Dr. Farhana Karim",
                "Pending department seminar", "CSE Seminar", nextMonday.plusDays(3),
                "15:45", "17:00", BookingStatus.PENDING);

        seedBookingNotifications(cr);

        if (!candidate.getRoles().contains(Role.CR.value()) && !pendingRolesRepo.existsByEmail(candidate.getEmail())) {
            PendingRole request = new PendingRole();
            request.setName(candidate.getName());
            request.setEmail(candidate.getEmail());
            request.setRole(Role.CR.value());
            pendingRolesRepo.save(request);
        }

        if (student.getId() == null || cr.getId() == null) {
            throw new IllegalStateException("Demo users were not persisted");
        }
    }

    private User ensureUser(
            String name,
            String email,
            String password,
            List<Role> roles,
            String program,
            Integer semester,
            Integer section) {
        User existing = userRepository.findByEmail(email.toLowerCase(Locale.ROOT));
        if (existing != null) {
            return existing;
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRoles(roles.stream().map(Role::value).toList());
        user.setProgram(program);
        user.setSemester(semester);
        user.setSection(section);
        return userService.createNewUser(user);
    }

    private Classroom ensureClassroom(String roomNumber, String building, int capacity, List<String> equipment) {
        Classroom existing = classroomRepo.findAll().stream()
                .filter(classroom -> classroom.getRoom_number().equalsIgnoreCase(roomNumber))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            return existing;
        }
        Classroom classroom = new Classroom();
        classroom.setRoom_number(roomNumber);
        classroom.setBuilding(building);
        classroom.setCapacity(capacity);
        classroom.setEquipment(new ArrayList<>(equipment));
        classroom.setIsAvailable(true);
        return classroomRepo.save(classroom);
    }

    private void ensureRoutine(
            Classroom classroom,
            String faculty,
            String courseCode,
            DayOfWeek day,
            String start,
            String end,
            String program,
            int semester,
            int section) {
        LocalTime startTime = LocalTime.parse(start);
        boolean exists = routineRepo.findAllByClassroomId(classroom.getId()).stream()
                .anyMatch(routine -> routine.getCourseCode().equals(courseCode)
                        && routine.getDay().equals(dayName(day))
                        && routine.getStartTime().equals(startTime)
                        && program.equals(routine.getProgram())
                        && Integer.valueOf(semester).equals(routine.getSemester())
                        && Integer.valueOf(section).equals(routine.getSection()));
        if (exists) {
            return;
        }
        Routine routine = new Routine();
        routine.setClassroomId(classroom.getId());
        routine.setFacultyName(faculty);
        routine.setCourseCode(courseCode);
        routine.setDay(dayName(day));
        routine.setStartTime(startTime);
        routine.setEndTime(LocalTime.parse(end));
        routine.setProgram(program);
        routine.setSemester(semester);
        routine.setSection(section);
        routineRepo.save(routine);
    }

    private void ensureBooking(
            User user,
            Classroom classroom,
            String facultyEmail,
            String facultyName,
            String reason,
            String courseCode,
            LocalDate date,
            String start,
            String end,
            BookingStatus status) {
        LocalTime startTime = LocalTime.parse(start);
        boolean exists = bookingRepo.findAllByUserId(user.getId()).stream()
                .anyMatch(booking -> booking.getClassroomId().equals(classroom.getId())
                        && booking.getBookingDate().equals(date)
                        && booking.getStartTime().equals(startTime)
                        && booking.getReason().equals(reason));
        if (exists) {
            return;
        }
        Booking booking = new Booking();
        booking.setClassroomId(classroom.getId());
        booking.setUserId(user.getId());
        booking.setFacultyEmail(facultyEmail);
        booking.setFacultyName(facultyName);
        booking.setReason(reason);
        booking.setCourseCode(courseCode);
        booking.setDay(dayName(date.getDayOfWeek()));
        booking.setBookingDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(LocalTime.parse(end));
        booking.setStatus(status);
        booking.setCreatedAt(LocalDate.now());
        if (status == BookingStatus.BOOKED) {
            booking.setApprovedAt(LocalDate.now());
        }
        bookingRepo.save(booking);
    }

    private void seedBookingNotifications(User user) {
        for (Booking booking : bookingRepo.findAllByUserId(user.getId())) {
            notificationService.bookingRequested(booking);
            if (booking.getStatus() == BookingStatus.BOOKED) {
                notificationService.bookingApproved(booking);
            } else if (booking.getStatus() == BookingStatus.REJECTED) {
                notificationService.bookingRejected(booking);
            }
        }
    }

    private String dayName(DayOfWeek day) {
        return day.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }
}
