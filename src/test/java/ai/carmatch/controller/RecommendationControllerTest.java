package ai.carmatch.controller;

import ai.carmatch.dto.RecommendationResult;
import ai.carmatch.dto.UserProfileResponse;
import ai.carmatch.model.UserPreferences;
import ai.carmatch.security.JwtAuthenticationFilter;
import ai.carmatch.security.JwtService;
import ai.carmatch.service.RecommendationService;
import ai.carmatch.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * /api/recommend takes no request body - it reads the caller's saved
 * UserPreferences via UserService, so these tests stub UserService rather
 * than posting preferences on the request.
 *
 * Security filters are disabled (addFilters = false) for this MVC slice test,
 * so the Authentication method argument is supplied directly via
 * .principal(...) rather than @WithMockUser - with filters off, Spring MVC's
 * Authentication resolution (backed by request.getUserPrincipal()) never runs.
 */
@WebMvcTest(RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecommendationControllerTest {

    private static final Authentication TEST_USER = new UsernamePasswordAuthenticationToken(
            "testUser", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RecommendationService recommendationService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpServletRequestBuilder recommend() {
        return get("/api/recommend").principal(TEST_USER);
    }

    private UserProfileResponse profileWithPreferences(UserPreferences preferences) {
        return new UserProfileResponse(1L, "testUser", "test@test.com", "Test", "User", null, null, preferences);
    }

    @Test
    void getRecommendations_ShouldReturnRecommendations() throws Exception {
        UserPreferences preferences = new UserPreferences(
                50000.0, "intermediate", "city", Arrays.asList("Toyota"), true
        );
        when(userService.getUserProfile("testUser")).thenReturn(profileWithPreferences(preferences));

        List<RecommendationResult> mockResults = Arrays.asList(
                new RecommendationResult("Corolla", "excellent value for money, good fuel efficiency", 85.5,
                        "Toyota", 25000.0, 2023, 139, 5.8, "Gasoline", true, "FWD", "White")
        );
        when(recommendationService.getRecommendations(eq(preferences))).thenReturn(mockResults);

        mockMvc.perform(recommend())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].model").value("Corolla"))
                .andExpect(jsonPath("$[0].brand").value("Toyota"))
                .andExpect(jsonPath("$[0].score").value(85.5))
                .andExpect(jsonPath("$[0].reason").value("excellent value for money, good fuel efficiency"));
    }

    @Test
    void getRecommendations_ShouldReturnEmptyListWhenNoMatches() throws Exception {
        UserPreferences preferences = new UserPreferences(
                10000.0, "novice", "city", Arrays.asList("Ferrari"), true
        );
        when(userService.getUserProfile("testUser")).thenReturn(profileWithPreferences(preferences));
        when(recommendationService.getRecommendations(eq(preferences))).thenReturn(Arrays.asList());

        mockMvc.perform(recommend())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getRecommendations_ShouldReturnBadRequestWhenNoPreferencesSet() throws Exception {
        when(userService.getUserProfile("testUser")).thenReturn(profileWithPreferences(null));

        mockMvc.perform(recommend())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getRecommendations_ShouldReturnNotFoundWhenUserMissing() throws Exception {
        when(userService.getUserProfile("testUser"))
                .thenThrow(new UsernameNotFoundException("testUser"));

        mockMvc.perform(recommend())
                .andExpect(status().isNotFound());
    }

    @Test
    void getRecommendations_ShouldReturnInternalServerErrorOnUnexpectedFailure() throws Exception {
        UserPreferences preferences = new UserPreferences(
                50000.0, "intermediate", "city", null, true
        );
        when(userService.getUserProfile("testUser")).thenReturn(profileWithPreferences(preferences));
        when(recommendationService.getRecommendations(any(UserPreferences.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(recommend())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void health_ShouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("CarMatchAI"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void info_ShouldReturnApplicationInfo() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("CarMatchAI"))
                .andExpect(jsonPath("$.description").value("AI-powered car recommendation service"));
    }
}
