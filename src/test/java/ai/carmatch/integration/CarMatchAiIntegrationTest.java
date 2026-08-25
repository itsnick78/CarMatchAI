package ai.carmatch.integration;

import ai.carmatch.dto.RecommendationResult;
import ai.carmatch.dto.UserLoginRequest;
import ai.carmatch.dto.UserPreferencesUpdateRequest;
import ai.carmatch.dto.UserRegistrationRequest;
import ai.carmatch.model.Car;
import ai.carmatch.repository.CarRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end test of the real auth flow: register -> login (AUTH_TOKEN cookie)
 * -> save preferences -> get recommendations. /api/recommend has no request
 * body; it always reads the caller's persisted UserPreferences.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CarMatchAiIntegrationTest {

    private static final String PASSWORD = "Test123567";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        carRepository.deleteAll();

        List<Car> testCars = Arrays.asList(
                new Car(null, "Toyota", "Corolla", 2023, 25000.0, 139, 5.8, "Gasoline", true, "FWD", "White"),
                new Car(null, "Honda", "Civic", 2023, 26000.0, 158, 6.2, "Gasoline", true, "FWD", "Silver"),
                new Car(null, "BMW", "M3", 2023, 75000.0, 473, 12.5, "Gasoline", false, "RWD", "White"),
                new Car(null, "Tesla", "Model 3", 2023, 45000.0, 283, 0.0, "Electric", true, "RWD", "White"),
                new Car(null, "Nissan", "Versa", 2023, 18000.0, 122, 5.5, "Gasoline", true, "FWD", "Blue")
        );
        carRepository.saveAll(testCars);
    }

    private Cookie registerAndLogin(String username, String email) throws Exception {
        UserRegistrationRequest registration = new UserRegistrationRequest();
        registration.setUsername(username);
        registration.setEmail(email);
        registration.setPassword(PASSWORD);
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isCreated());

        UserLoginRequest login = new UserLoginRequest(email, PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
        assertNotNull(setCookieHeader, "Login response must set the AUTH_TOKEN cookie");
        String token = setCookieHeader.split(";")[0].split("=", 2)[1];
        return new Cookie("AUTH_TOKEN", token);
    }

    private void savePreferences(Cookie authCookie, Double budget, String experience, String useCase,
                                  List<String> brandPreferences, boolean fuelEconomyPriority) throws Exception {
        UserPreferencesUpdateRequest request = new UserPreferencesUpdateRequest(
                budget, experience, useCase, brandPreferences, fuelEconomyPriority);
        mockMvc.perform(post("/api/users/preferences")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getRecommendations_ShouldReturnFilteredResults() throws Exception {
        Cookie authCookie = registerAndLogin("filteredUser", "filtered@test.com");
        savePreferences(authCookie, 50000.0, "intermediate", "city", Arrays.asList("Toyota", "Honda"), true);

        String response = mockMvc.perform(get("/api/recommend").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RecommendationResult[] results = objectMapper.readValue(response, RecommendationResult[].class);
        assertTrue(results.length > 0);
        assertTrue(results.length <= 5);
        for (RecommendationResult result : results) {
            assertTrue(result.getPrice() <= 50000.0);
            assertTrue(result.isCompact());
            assertTrue(result.getFuelConsumption() <= 7.0);
            assertTrue(Arrays.asList("Toyota", "Honda").contains(result.getBrand()));
            assertTrue(result.getScore() >= 0);
            assertNotNull(result.getReason());
        }
    }

    @Test
    void getRecommendations_ShouldFilterByNoviceExperience() throws Exception {
        Cookie authCookie = registerAndLogin("noviceUser", "novice@test.com");
        savePreferences(authCookie, 50000.0, "novice", "city", null, true);

        String response = mockMvc.perform(get("/api/recommend").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RecommendationResult[] results = objectMapper.readValue(response, RecommendationResult[].class);
        for (RecommendationResult result : results) {
            assertTrue(result.getHorsePower() <= 150);
        }
    }

    @Test
    void getRecommendations_ShouldReturnEmptyListForStrictCriteria() throws Exception {
        Cookie authCookie = registerAndLogin("strictUser", "strict@test.com");
        savePreferences(authCookie, 10000.0, "novice", "city", Arrays.asList("Ferrari"), true);

        mockMvc.perform(get("/api/recommend").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getRecommendations_ShouldSortByScoreDescending() throws Exception {
        Cookie authCookie = registerAndLogin("sortUser", "sort@test.com");
        savePreferences(authCookie, 50000.0, "intermediate", "city", null, true);

        String response = mockMvc.perform(get("/api/recommend").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RecommendationResult[] results = objectMapper.readValue(response, RecommendationResult[].class);
        if (results.length > 1) {
            for (int i = 0; i < results.length - 1; i++) {
                assertTrue(results[i].getScore() >= results[i + 1].getScore(),
                        "Results should be sorted by score in descending order");
            }
        }
    }

    @Test
    void getRecommendations_ShouldReturnUnauthorizedWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/recommend"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRecommendations_ShouldReturnBadRequestWhenPreferencesNotSet() throws Exception {
        Cookie authCookie = registerAndLogin("noPrefsUser", "noprefs@test.com");

        mockMvc.perform(get("/api/recommend").cookie(authCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    void health_ShouldReturnUpStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("CarMatchAI"));
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
