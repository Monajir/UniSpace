package com.techManiacs.UniSpace.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techManiacs.UniSpace.domain.BookingStatus;
import com.techManiacs.UniSpace.domain.Role;
import com.techManiacs.UniSpace.dto.BookingCreateRequest;
import com.techManiacs.UniSpace.dto.BookingDto;
import com.techManiacs.UniSpace.dto.UserCreateRequest;
import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiMapperTests {
    private final ApiMapper mapper = new ApiMapper();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void bookingRequestMapsDirectlyToNativePostgresqlTypes() {
        UUID classroomId = UUID.randomUUID();
        BookingCreateRequest request = new BookingCreateRequest(
                classroomId.toString(), "faculty@iut-dhaka.edu", "Extra class", "CSE-401",
                "Monday", LocalDate.of(2026, 9, 7), LocalTime.of(8, 30), LocalTime.of(9, 30));

        Booking booking = mapper.toBooking(request);

        assertThat(booking.getClassroomId()).isEqualTo(classroomId);
        assertThat(booking.getBookingDate()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(booking.getStartTime()).isEqualTo(LocalTime.of(8, 30));
    }

    @Test
    void bookingDtoKeepsStableStringIdsAndCanonicalJsonValues() throws Exception {
        UUID bookingId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Booking booking = new Booking(
                bookingId, classroomId, userId, "faculty@iut-dhaka.edu", "Demo Faculty",
                "Extra class", "CSE-401", "Monday", LocalDate.of(2026, 9, 7),
                LocalTime.of(8, 30), LocalTime.of(9, 30), BookingStatus.BOOKED,
                LocalDate.of(2026, 8, 28), LocalDate.of(2026, 8, 29));

        BookingDto dto = mapper.toBookingDto(booking);
        String json = objectMapper.writeValueAsString(dto);

        assertThat(dto.id()).isEqualTo(bookingId.toString());
        assertThat(dto.classroomId()).isEqualTo(classroomId.toString());
        assertThat(dto.userId()).isEqualTo(userId.toString());
        assertThat(json).contains("\"status\":\"booked\"")
                .contains("\"booking_date\":\"2026-09-07\"")
                .contains("\"start_time\":\"08:30\"");
    }

    @Test
    void userRequestMapsRoleEnumsToPersistenceValues() {
        UserCreateRequest request = new UserCreateRequest(
                "Demo Admin", "admin@iut-dhaka.edu", "password",
                List.of(Role.ADMIN, Role.FACULTY), null, null, null);

        User user = mapper.toUser(request);

        assertThat(user.getRoles()).containsExactly("ADMIN", "FACULTY");
    }
}

