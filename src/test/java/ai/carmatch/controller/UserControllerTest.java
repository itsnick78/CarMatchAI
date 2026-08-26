package ai.carmatch.controller;

import ai.carmatch.dto.NaturalLanguagePreferencesRequest;
import ai.carmatch.dto.UserPreferencesUpdateRequest;
import ai.carmatch.dto.UserProfileResponse;
import ai.carmatch.model.UserPreferences;
import ai.carmatch.security.JwtAuthenticationFilter;
import ai.carmatch.security.JwtService;
import ai.carmatch.service.PreferenceExtractionService;
import ai.carmatch.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers only POST /api/users/preferences/from-text: PreferenceExtractionService
 * is mocked here, since its own unit test (PreferenceExtractionServiceTest)
 * already covers the LLM-facing logic - this test is about the controller's
 * status-code/error mapping, not about the extraction itself.
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final Authentication TEST_USER = new UsernamePasswordAuthenticationToken(
            "testUser", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PreferenceExtractionService preferenceExtractionService;

    private MockHttpServletRequestBuilder fromText(String text) throws Exception {
        NaturalLanguagePreferencesRequest request = new NaturalLanguagePreferencesRequest(text);
        return post("/api/users/preferences/from-text")
                .principal(TEST_USER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));
    }

    @Test
    void createPreferencesFromText_ShouldReturnCreatedOnSuccess() throws Exception {
        UserPreferencesUpdateRequest extracted = new UserPreferencesUpdateRequest(
                40000.0, "intermediate", "city", Arrays.asList("Toyota"), true);
        UserPreferences saved = new UserPreferences(40000.0, "intermediate", "city", Arrays.asList("Toyota"), true);
        UserProfileResponse profile = new UserProfileResponse(1L, "testUser", "test@test.com", "Test", "User", null, null, saved);

        when(preferenceExtractionService.extract(anyString())).thenReturn(extracted);
        when(userService.updateUserPreferences(eq("testUser"), eq(extracted))).thenReturn(profile);

        mockMvc.perform(fromText("недорогая экономичная машина для города"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Preferences extracted and saved successfully"))
                .andExpect(jsonPath("$.preferences.budget").value(40000.0))
                .andExpect(jsonPath("$.preferences.useCase").value("city"));
    }

    @Test
    void createPreferencesFromText_ShouldReturnBadRequestWhenExtractionFails() throws Exception {
        when(preferenceExtractionService.extract(anyString()))
                .thenThrow(new IllegalArgumentException("Could not extract preferences from the provided text"));

        mockMvc.perform(fromText("gibberish"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createPreferencesFromText_ShouldReturnBadRequestWhenTextBlank() throws Exception {
        mockMvc.perform(fromText(" "))
                .andExpect(status().isBadRequest());
    }
}
