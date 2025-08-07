package demo.todolist.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.todolist.repository.TaskRepository;
import demo.todolist.repository.UserRepository;
import demo.todolist.web.dto.RegisterRequest;
import demo.todolist.web.dto.TaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jackson.serialization.write-dates-as-timestamps=false",
})
class TaskControllerIT {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;
    private static final String DEFAULT_PASSWORD = "Str0ngPass!1";
    private static final AtomicInteger userCounter = new AtomicInteger(0);

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserRepository userRepo;
    @Autowired private TaskRepository taskRepo;

    private MockMvc mockMvc;

    /* ---------- Test User Management ---------- */

    record TestUser(String username, String email, String token) {}

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        taskRepo.deleteAll();
        userRepo.deleteAll();
        userCounter.set(0);
    }

    private TestUser createTestUser() throws Exception {
        int id = userCounter.incrementAndGet();
        String username = "user" + id;
        String email = username + "@test.com";

        return createTestUser(username, email, DEFAULT_PASSWORD);
    }

    private TestUser createTestUser(String username, String email, String password) throws Exception {
        // Register
        mockMvc.perform(post("/api/auth/register")
                        .contentType(JSON)
                        .content(toJson(new RegisterRequest(username, email, password))))
                .andExpect(status().isCreated());

        // Login and get token
        String loginJson = toJson(Map.of("username", username, "password", password));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String tokenJson = result.getResponse().getContentAsString();
        JsonNode root = mapper.readTree(tokenJson);
        String token = root.get("accessToken").asText();

        return new TestUser(username, email, token);
    }

    private String toJson(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    /* ---------- Task Helper Methods ---------- */

    private TaskRequest simpleTask(String title) {
        return new TaskRequest(title, null, true);
    }

    private TaskRequest fullTask(String title, String description, boolean active) {
        return new TaskRequest(title, description, active);
    }

    private String createTaskAndGetId(TestUser user, TaskRequest task) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(user.token()))
                        .contentType(JSON)
                        .content(toJson(task)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode node = mapper.readTree(responseBody);
        return node.get("id").asText();
    }

    /* ---------- Tests ---------- */

    @Nested
    @DisplayName("/api/tasks")
    class TasksEndpoint {

        @Test
        @DisplayName("POST /api/tasks създава задача")
        void createTask_createsTask() throws Exception {
            TestUser user = createTestUser();
            TaskRequest task = fullTask("Buy milk", "Remember to buy milk after work", true);

            mockMvc.perform(post("/api/tasks")
                            .header("Authorization", bearer(user.token()))
                            .contentType(JSON)
                            .content(toJson(task)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.title").value("Buy milk"))
                    .andExpect(jsonPath("$.ownerUsername").value(user.username()))
                    .andExpect(jsonPath("$.isActive").value(true)); // "isActive"

            assertThat(taskRepo.count()).isEqualTo(1);
        }


        @Test
        @DisplayName("GET /api/tasks връща само моите задачи")
        void listTasks_returnsOwnTasks() throws Exception {
            TestUser alice = createTestUser();
            TestUser bob = createTestUser();

            // Alice създава задача
            createTaskAndGetId(alice, simpleTask("Alice's Task"));

            // Bob създава задача
            createTaskAndGetId(bob, simpleTask("Bob's Task"));

            // Bob вижда само своята задача
            mockMvc.perform(get("/api/tasks")
                            .header("Authorization", bearer(bob.token())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title").value("Bob's Task"));
        }

        @Test
        @DisplayName("PUT /api/tasks/{id} актуализира задача")
        void updateTask_updatesData() throws Exception {
            TestUser user = createTestUser();
            String taskId = createTaskAndGetId(user, simpleTask("Old Title"));

            TaskRequest updatedTask = fullTask("New Title", "Updated description", false);

            mockMvc.perform(put("/api/tasks/{id}", taskId)
                            .header("Authorization", bearer(user.token()))
                            .contentType(JSON)
                            .content(toJson(updatedTask)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("New Title"))
                    .andExpect(jsonPath("$.description").value("Updated description"))
                    .andExpect(jsonPath("$.isActive").value(false)); // "isActive"
        }

        @Test
        @DisplayName("DELETE /api/tasks/{id} трие задача")
        void deleteTask_removes() throws Exception {
            TestUser user = createTestUser();
            String taskId = createTaskAndGetId(user, simpleTask("Task to delete"));

            mockMvc.perform(delete("/api/tasks/{id}", taskId)
                            .header("Authorization", bearer(user.token())))
                    .andExpect(status().isNoContent());

            assertThat(taskRepo.existsById(UUID.fromString(taskId))).isFalse();
        }



    }
}