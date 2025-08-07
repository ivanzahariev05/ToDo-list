package demo.todolist.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.todolist.repository.UserRepository;
import demo.todolist.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class
AuthControllerIT {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper mapper;
    private static final String BASE = "/api/auth/register";
    private static final MediaType JSON = MediaType.APPLICATION_JSON;


    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    private String jsonRegisterBody(String username, String email, String password) {
        return String.format("""
            {
              \"username\": \"%s\",
              \"email\": \"%s\",
              \"password\": \"%s\"
            }
            """, username, email, password);
    }

    @Test
    void register_createsUser() throws Exception {
        var body = mapper.writeValueAsString(new RegisterRequest(
                "john", "john@example.com", "Str0ngPass!1"));

        mvc.perform(post(BASE).contentType(JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }


    @Test
    void register_fails_whenEmailExists() throws Exception {

        var first = mapper.writeValueAsString(new RegisterRequest(
                "alice", "alice@mail.com", "Str0ngPass!1"));
        mvc.perform(post(BASE).contentType(JSON).content(first))
                .andExpect(status().isCreated());

        var dup = mapper.writeValueAsString(new RegisterRequest(
                "bob", "alice@mail.com", "Str0ngPass!1"));

        mvc.perform(post(BASE).contentType(JSON).content(dup))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email")
                        .value("This email is already in use!"));
    }

    @Nested
    @DisplayName("/api/auth/login")
    class LoginEndpoint {

        @Test
        void login_returnsTokens() throws Exception {
            // Регистрираме потребител през endpoint‑a, за да минем през същата логика (encode парола и роля)
            String username = "mark";
            String password = "Secur3Pass!1";
            mvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRegisterBody(username, "mark@example.com", password)))
                    .andExpect(status().isCreated());

            // Login
            String loginBody = String.format("""
                {
                  \"username\": \"%s\",
                  \"password\": \"%s\"
                }
                """, username, password);

            String json = mvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            JsonNode root = mapper.readTree(json);
            assertThat(root.get("accessToken").asText()).hasSizeGreaterThan(30);
            assertThat(root.get("refreshToken").asText()).hasSizeGreaterThan(30);
        }
    }
}
