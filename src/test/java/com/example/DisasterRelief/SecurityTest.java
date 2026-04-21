package com.example.DisasterRelief;

import com.example.DisasterRelief.repository.UserRepository;
import com.example.DisasterRelief.security.JwtUtil;
import com.example.DisasterRelief.service.UserService;
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

import com.example.DisasterRelief.service.EmailService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies that security rules are enforced:
 * <ul>
 *   <li>Unauthenticated callers are rejected (401) from protected endpoints.</li>
 *   <li>USER role can call {@code POST /api/send-request} but not user management.</li>
 *   <li>ADMIN role can call all user management endpoints.</li>
 *   <li>{@code POST /api/users} (self-registration) and {@code POST /api/auth/login} are public.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private EmailService emailService;

    @AfterEach
    void cleanup() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        userRepository.deleteAll();
    }

    // ── Unauthenticated access ────────────────────────────────────────────────

    @Test
    void unauthenticatedAccessToSendRequestReturns401() throws Exception {
        mockMvc.perform(post("/api/send-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"address\":\"Addr\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedAccessToUsersReturns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    // ── Public endpoints ──────────────────────────────────────────────────────

    @Test
    void selfRegistrationIsPublic() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"email\":\"new@example.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        userService.createUser("loginuser", "login@example.com", "USER", "mypassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"loginuser\",\"password\":\"mypassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginWithBadCredentialsReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── Role-based access ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    void userRoleCannotListAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRoleCanListAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRoleCannotDeleteUsers() throws Exception {
        mockMvc.perform(delete("/api/users/some-id"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRoleCanDeleteNonExistentUser() throws Exception {
        mockMvc.perform(delete("/api/users/nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    // ── JWT token authentication ──────────────────────────────────────────────

    @Test
    void jwtTokenGrantsAccessToProtectedEndpoint() throws Exception {
        userService.createUser("jwtuser", "jwt@example.com", "USER", "jwtpassword");
        String token = jwtUtil.generateToken("jwtuser", "USER");

        mockMvc.perform(post("/api/send-request")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"JWT User\",\"address\":\"Addr\",\"towel\":1,\"instantNoodles\":1,\"tissuePaper\":1,\"water\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void invalidJwtTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/send-request")
                        .header("Authorization", "Bearer this.is.not.a.valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"address\":\"Addr\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminJwtTokenGrantsAccessToUserManagement() throws Exception {
        String token = jwtUtil.generateToken("admin", "ADMIN");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
