package com.example.DisasterRelief;

import com.example.DisasterRelief.service.IdempotencyService;
import com.example.DisasterRelief.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IdempotencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IdempotencyService idempotencyService;

    @MockBean
    private EmailService emailService;

    private static final String REQUEST_BODY = """
            {"name":"Alice","address":"123 St","towel":1,"instantNoodles":2,"tissuePaper":3,"water":4}
            """;

    @Test
    @WithMockUser(roles = "USER")
    void sameIdempotencyKeyReturnsIdenticalResponseWithoutResendingEmail() throws Exception {
        String key = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/send-request")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Request received successfully!"));

        mockMvc.perform(post("/api/send-request")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Request received successfully!"));

        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void differentIdempotencyKeysTriggerSeparateProcessing() throws Exception {
        String key1 = UUID.randomUUID().toString();
        String key2 = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/send-request")
                        .header("Idempotency-Key", key1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/send-request")
                        .header("Idempotency-Key", key2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk());

        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void requestWithoutIdempotencyKeyAlwaysProcesses() throws Exception {
        mockMvc.perform(post("/api/send-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/send-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk());

        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void idempotencyServiceStoresAndRetrievesResponse() {
        String key = UUID.randomUUID().toString();
        Map<String, String> response = Map.of("message", "Test response");

        assertTrue(idempotencyService.findByKey(key).isEmpty());

        idempotencyService.store(key, response);

        assertTrue(idempotencyService.findByKey(key).isPresent());
        assertEquals(response, idempotencyService.findByKey(key).get());
    }
}
