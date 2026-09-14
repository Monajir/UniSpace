package com.techManiacs.UniSpace.mapper;

import com.techManiacs.UniSpace.domain.Role;
import com.techManiacs.UniSpace.dto.*;
import com.techManiacs.UniSpace.model.*;
import com.techManiacs.UniSpace.utils.PendingResponse;
import com.techManiacs.UniSpace.utils.RoomScheduleResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ApiMapper {

    public Booking toBooking(BookingCreateRequest request) {
        Booking booking = new Booking();
        booking.setClassroomId(UUID.fromString(request.classroomId()));
        booking.setFacultyEmail(request.facultyEmail());
        booking.setReason(request.reason());
        booking.setCourseCode(request.courseCode());
        booking.setDay(request.day());
        booking.setBookingDate(request.bookingDate());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        return booking;
    }

    public BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                id(booking.getId()),
                id(booking.getClassroomId()),
                id(booking.getUserId()),
                booking.getFacultyEmail(),
                booking.getFacultyName(),
                booking.getReason(),
                booking.getCourseCode(),
                booking.getDay(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getApprovedAt());
    }

    public RoutineDto toRoutineDto(Routine routine) {
        return new RoutineDto(
                id(routine.getId()),
                id(routine.getClassroomId()),
                routine.getFacultyName(),
                routine.getCourseCode(),
                routine.getDay(),
                routine.getStartTime(),
                routine.getEndTime(),
                routine.getProgram(),
                routine.getSemester(),
                routine.getSection());
    }

    public RoomScheduleDto toRoomScheduleDto(RoomScheduleResponse schedule) {
        List<RoutineDto> regular = schedule.getRegular().stream().map(this::toRoutineDto).toList();
        List<BookingDto> extras = schedule.getExtras().stream().map(this::toBookingDto).toList();
        return new RoomScheduleDto(regular, extras);
    }

    public ClassroomDto toClassroomDto(Classroom classroom) {
        return new ClassroomDto(
                id(classroom.getId()),
                classroom.getRoom_number(),
                classroom.getBuilding(),
                classroom.getCapacity(),
                classroom.getEquipment(),
                classroom.getIsAvailable());
    }

    public ProfileDto toProfileDto(Profile profile) {
        if (profile == null) {
            return null;
        }
        return new ProfileDto(
                id(profile.getId()),
                id(profile.getUserId()),
                profile.getFull_name(),
                profile.getEmail(),
                profile.getProgram(),
                profile.getSemester(),
                profile.getRole() == null ? null : Role.fromValue(profile.getRole()));
    }

    public UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        List<Role> roles = user.getRoles() == null
                ? List.of()
                : user.getRoles().stream().map(Role::fromValue).toList();
        return new UserDto(
                id(user.getId()),
                user.getName(),
                user.getEmail(),
                roles,
                user.getProgram(),
                user.getSemester(),
                user.getSection());
    }

    public User toUser(UserCreateRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setRoles(request.roles() == null ? null : request.roles().stream().map(Role::value).toList());
        user.setProgram(request.program());
        user.setSemester(request.semester());
        user.setSection(request.section());
        return user;
    }

    public PendingRoleDto toPendingRoleDto(PendingRole pendingRole) {
        return new PendingRoleDto(
                id(pendingRole.getId()),
                pendingRole.getEmail(),
                Role.fromValue(pendingRole.getRole()),
                pendingRole.getName());
    }

    public PendingBookingDto toPendingBookingDto(PendingResponse response) {
        return new PendingBookingDto(
                toBookingDto(response.getPending()),
                response.getUser_name(),
                response.getRoom_number());
    }

    public NotificationDto toNotificationDto(Notification notification) {
        return new NotificationDto(
                id(notification.getId()),
                id(notification.getBookingId()),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt());
    }

    private String id(UUID id) {
        return id == null ? null : id.toString();
    }
}
