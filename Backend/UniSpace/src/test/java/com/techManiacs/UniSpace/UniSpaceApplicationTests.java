package com.techManiacs.UniSpace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techManiacs.UniSpace.model.User;
import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.model.Routine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest(properties = {
        "jwt.secret=test-only-secret-that-is-at-least-32-bytes",
        "spring.datasource.url=jdbc:h2:mem:unispace;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
class UniSpaceApplicationTests {
    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping")
    private org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping handlerMapping;

    @Test
    void exposesNormalizedApiRoutesWithoutLegacyAliases() {
        Set<String> routes = handlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(mapping -> mapping.getPatternValues().stream())
                .collect(Collectors.toSet());

        assertThat(routes).contains(
                "/api/auth/login", "/api/auth/me",
                "/api/classrooms", "/api/classrooms/{id}/schedule",
                "/api/bookings", "/api/bookings/assigned-to-me",
                "/api/me/bookings", "/api/me/routine",
                "/api/role-requests", "/api/users");
        assertThat(routes).doesNotContain(
                "/public/login", "/api/bookings/room/book",
                "/api/bookings/classSchedule/{id}", "/api/bookings/classSchedule/next/{id}",
                "/api/bookings/faculty", "/roles/all", "/roles/create/user",
                "/student/my/bookings", "/student/my/routine", "/student/role-request");
    }
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    private com.techManiacs.UniSpace.service.ClassroomCatalogueCache classroomCache;

    @Autowired
    private com.techManiacs.UniSpace.repository.ClassroomRepo classroomRepo;

    @Autowired
    private com.techManiacs.UniSpace.repository.RoutineRepo routineRepo;

    @Test
    void persistedClassroomChangesInvalidateCatalogue() {
        var classroom = new com.techManiacs.UniSpace.model.Classroom();
        classroom.setRoom_number("CACHE-TEST");
        classroom.setBuilding("Test");
        classroom.setCapacity(40);
        classroom = classroomRepo.saveAndFlush(classroom);
        org.mockito.Mockito.verify(classroomCache, org.mockito.Mockito.atLeastOnce()).evict();
        org.mockito.Mockito.clearInvocations(classroomCache);
        classroom.setCapacity(50);
        classroom = classroomRepo.saveAndFlush(classroom);
        org.mockito.Mockito.verify(classroomCache, org.mockito.Mockito.atLeastOnce()).evict();
        org.mockito.Mockito.clearInvocations(classroomCache);
        classroom.setEquipment(new java.util.ArrayList<>(List.of("Projector")));
        classroom = classroomRepo.saveAndFlush(classroom);
        org.mockito.Mockito.verify(classroomCache, org.mockito.Mockito.atLeastOnce()).evict();
        org.mockito.Mockito.clearInvocations(classroomCache);
        classroomRepo.delete(classroom);
        org.mockito.Mockito.verify(classroomCache, org.mockito.Mockito.atLeastOnce()).evict();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void classroomScheduleAcceptsAnArbitraryFutureWeekStart() throws Exception {
        mockMvc.perform(get("/api/classrooms/{id}/schedule", UUID.randomUUID())
                        .param("weekStart", "2030-01-07"))
                .andExpect(status().isOk());
    }

    @Test
    void classroomScheduleRejectsAnInvalidWeekStart() throws Exception {
        mockMvc.perform(get("/api/classrooms/{id}/schedule", UUID.randomUUID())
                        .param("weekStart", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedUserCreationIsRejected() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Attacker\",\"email\":\"attacker@iut-dhaka.edu\",\"password\":\"password\",\"roles\":[\"ADMIN\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void classroomCreationRequiresAnAdministrator() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "room_number", "SECURITY-" + UUID.randomUUID(),
                "building", "Academic Building",
                "capacity", 40,
                "equipment", List.of("Projector")));

        mockMvc.perform(post("/api/classrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/classrooms")
                        .with(user("student@iut-dhaka.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanCreateAClassroom() throws Exception {
        String roomNumber = "ADMIN-" + UUID.randomUUID();
        String payload = objectMapper.writeValueAsString(Map.of(
                "room_number", "  " + roomNumber + "  ",
                "building", "  Academic Building  ",
                "capacity", 48,
                "equipment", List.of("Projector", " Whiteboard ", "")));

        mockMvc.perform(post("/api/classrooms")
                        .with(user("admin@iut-dhaka.edu").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.room_number").value(roomNumber))
                .andExpect(jsonPath("$.building").value("Academic Building"))
                .andExpect(jsonPath("$.capacity").value(48))
                .andExpect(jsonPath("$.is_available").value(true))
                .andExpect(jsonPath("$.equipment.length()").value(2));
    }

    @Test
    void classroomCreationValidatesInputAndDuplicateRoomNumbers() throws Exception {
        var admin = user("admin@iut-dhaka.edu").roles("ADMIN");
        mockMvc.perform(post("/api/classrooms")
                        .with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"room_number\":\" \",\"building\":\"Academic\",\"capacity\":0}"))
                .andExpect(status().isBadRequest());

        String roomNumber = "DUPLICATE-" + UUID.randomUUID();
        String payload = objectMapper.writeValueAsString(Map.of(
                "room_number", roomNumber,
                "building", "Academic",
                "capacity", 30,
                "equipment", List.of()));

        mockMvc.perform(post("/api/classrooms")
                        .with(user("admin@iut-dhaka.edu").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/classrooms")
                        .with(user("admin@iut-dhaka.edu").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void administratorCanUpdateAndDeleteAnEmptyClassroom() throws Exception {
        Classroom classroom = new Classroom();
        classroom.setRoom_number("EDIT-" + UUID.randomUUID());
        classroom.setBuilding("Old Building");
        classroom.setCapacity(20);
        classroom.setEquipment(new java.util.ArrayList<>());
        classroom.setIsAvailable(true);
        classroom = classroomRepo.saveAndFlush(classroom);

        String updatedRoomNumber = "UPDATED-" + UUID.randomUUID();
        String payload = objectMapper.writeValueAsString(Map.of(
                "room_number", updatedRoomNumber,
                "building", "New Building",
                "capacity", 60,
                "equipment", List.of("Projector", "Microphone")));

        mockMvc.perform(patch("/api/classrooms/{id}", classroom.getId())
                        .with(user("admin@iut-dhaka.edu").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.room_number").value(updatedRoomNumber))
                .andExpect(jsonPath("$.building").value("New Building"))
                .andExpect(jsonPath("$.capacity").value(60));

        mockMvc.perform(delete("/api/classrooms/{id}", classroom.getId())
                        .with(user("admin@iut-dhaka.edu").roles("ADMIN")))
                .andExpect(status().isNoContent());

        assertThat(classroomRepo.findById(classroom.getId())).isEmpty();
    }

    @Test
    void classroomUpdateAndDeletionRequireAnAdministrator() throws Exception {
        UUID classroomId = UUID.randomUUID();
        String payload = objectMapper.writeValueAsString(Map.of(
                "room_number", "FORBIDDEN",
                "building", "Academic",
                "capacity", 40,
                "equipment", List.of()));

        mockMvc.perform(patch("/api/classrooms/{id}", classroomId)
                        .with(user("student@iut-dhaka.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/classrooms/{id}", classroomId)
                        .with(user("student@iut-dhaka.edu").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void classroomWithRoutineHistoryCannotBeDeleted() throws Exception {
        Classroom classroom = new Classroom();
        classroom.setRoom_number("HISTORY-" + UUID.randomUUID());
        classroom.setBuilding("Academic");
        classroom.setCapacity(30);
        classroom.setEquipment(new java.util.ArrayList<>());
        classroom.setIsAvailable(true);
        classroom = classroomRepo.saveAndFlush(classroom);

        Routine routine = new Routine();
        routine.setClassroomId(classroom.getId());
        routine.setFacultyName("Test Faculty");
        routine.setCourseCode("CSE 9999");
        routine.setDay("Monday");
        routine.setStartTime(LocalTime.of(9, 0));
        routine.setEndTime(LocalTime.of(10, 15));
        routineRepo.saveAndFlush(routine);

        mockMvc.perform(delete("/api/classrooms/{id}", classroom.getId())
                        .with(user("admin@iut-dhaka.edu").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Classrooms with booking or routine history cannot be deleted"));
    }

    @Test
    void userSerializationNeverIncludesPassword() throws Exception {
        User user = new User(null, "Test User", "hashed-password", "test@iut-dhaka.edu",
                List.of("STUDENT"), null, null, null);

        String json = objectMapper.writeValueAsString(user);

        assertThat(json).doesNotContain("password").doesNotContain("hashed-password");
    }

    @Test
    void facultyCanLoadOnlyTheFacultyDashboardApi() throws Exception {
        mockMvc.perform(get("/api/bookings/assigned-to-me")
                        .with(user("faculty@iut-dhaka.edu").roles("FACULTY")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/bookings/pending")
                        .with(user("faculty@iut-dhaka.edu").roles("FACULTY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCannotLoadFacultyDashboardApi() throws Exception {
        mockMvc.perform(get("/api/bookings/assigned-to-me")
                        .with(user("student@iut-dhaka.edu").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }
}
