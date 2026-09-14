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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void unauthenticatedUserCreationIsRejected() throws Exception {
        mockMvc.perform(post("/roles/create/user")
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
        mockMvc.perform(get("/api/bookings/faculty")
                        .with(user("faculty@iut-dhaka.edu").roles("FACULTY")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/bookings/pending")
                        .with(user("faculty@iut-dhaka.edu").roles("FACULTY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCannotLoadFacultyDashboardApi() throws Exception {
        mockMvc.perform(get("/api/bookings/faculty")
                        .with(user("student@iut-dhaka.edu").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }
}
