package demo.todolist.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.todolist.repository.UserRepository;
import demo.todolist.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AdminUserControllerIT {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;
    private static final String PASSWORD = "Str0ngPass!1";

    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository userRepo;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        userRepo.deleteAll();
    }

    record TestUser(UUID id, String username, String token) {}

    private String toJson(Object o) throws Exception { return mapper.writeValueAsString(o); }
    private String auth(String token) { return "Bearer " + token; }

    private TestUser createUser(boolean admin) throws Exception {
        String username = (admin ? "admin" : "user") + UUID.randomUUID().toString().substring(0,5);
        String email = username + "@mail.com";

        // register
        mockMvc.perform(post("/api/auth/register")
                        .contentType(JSON)
                        .content(toJson(new RegisterRequest(username, email, PASSWORD))))
                .andExpect(status().isCreated());

        // login
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(JSON)
                        .content(toJson(Map.of("username", username, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        String token = mapper.readTree(res.getResponse().getContentAsString())
                .get("accessToken").asText();

        UUID id = userRepo.findUserByEmail(email).orElseThrow().getId();
        return new TestUser(id, username, token);
    }

    @Nested
    @DisplayName("/api/admin/users")
    class AdminEndpoints {

        @Test
        @DisplayName("POST /{id}/promote – промотира user в admin")
        void promoteUser_success() throws Exception {
            TestUser admin = createUser(true);
            TestUser normal = createUser(false);

            // Промоция
            mockMvc.perform(post("/api/admin/users/{id}/promote", normal.id())
                            .header("Authorization", auth(admin.token())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ADMIN"));

            assertThat(userRepo.findById(normal.id()).get().getRole().name())
                    .isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("DELETE /{id} – изтрива user")
        void deleteUser_success() throws Exception {
            TestUser admin = createUser(true);
            TestUser normal = createUser(false);

            mockMvc.perform(delete("/api/admin/users/{id}", normal.id())
                            .header("Authorization", auth(admin.token())))
                    .andExpect(status().isNoContent());

            assertThat(userRepo.existsById(normal.id())).isFalse();
        }


    }
}
