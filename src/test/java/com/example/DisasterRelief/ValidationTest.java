package com.example.DisasterRelief;

import com.example.DisasterRelief.service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies that jakarta.validation constraints are enforced on all API endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private EmailService emailService;

    @AfterEach
    void cleanup() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        File usersFile = new File("users.json");
        if (usersFile.exists() && !usersFile.delete()) {
            usersFile.deleteOnExit();
        }
    }

    // ── POST /api/send-request ────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    void sendRequestWithMissingNameReturns400() throws Exception {
        mockMvc.perform(post("/api/send-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"123 Main St\",\"towel\":1,\"instantNoodles\":1,\"tissuePaper\":1,\"water\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void sendRequestWithMissingAddressReturns400() throws Exception {
        mockMvc.perform(post("/api/send-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"towel\":1,\"instantNoodles\":1,\"tissuePaper\":1,\"water\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void sendRequestWithNegativeTowelQuantityReturns400() throws Exception {
        mockMvc.perform(post("/api/send-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"address\":\"123 Main St\",\"towel\":-1,\"instantNoodles\":1,\"tissuePaper\":1,\"water\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void sendRequestWithValidDataReturns200() throws Exception {
        mockMvc.perform(post("/api/send-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"address\":\"123 Main St\",\"towel\":2,\"instantNoodles\":3,\"tissuePaper\":1,\"water\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Request received successfully!"));
    }

    // ── POST /api/users (self-registration) ──────────────────────────────────

    @Test
    void registerWithMissingUsernameReturns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void registerWithInvalidEmailReturns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"email\":\"not-an-email\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void registerWithShortPasswordReturns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"email\":\"alice@example.com\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void registerWithShortUsernameReturns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ab\",\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void registerWithValidDataReturns200() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    // ── PUT /api/users/{id} ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserWithInvalidRoleReturns400() throws Exception {
        mockMvc.perform(put("/api/users/some-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"email\":\"alice@example.com\",\"role\":\"SUPERADMIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserWithInvalidEmailReturns400() throws Exception {
        mockMvc.perform(put("/api/users/some-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"email\":\"bad-email\",\"role\":\"USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @Test
    void loginWithBlankUsernameReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"mypassword\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void loginWithBlankPasswordReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }
}
