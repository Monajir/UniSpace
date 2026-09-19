package com.techManiacs.UniSpace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techManiacs.UniSpace.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
    void unauthenticatedUserCreationIsRejected() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Attacker\",\"email\":\"attacker@iut-dhaka.edu\",\"password\":\"password\",\"roles\":[\"ADMIN\"]}"))
                .andExpect(status().isForbidden());
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
