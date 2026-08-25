package ai.carmatch.service;

import ai.carmatch.model.Car;
import ai.carmatch.dto.RecommendationResult;
import ai.carmatch.model.UserPreferences;
import ai.carmatch.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private List<Car> sampleCars;
    private UserPreferences samplePreferences;

    @BeforeEach
    void setUp() {
        // Create sample cars
        sampleCars = Arrays.asList(
                new Car(1L, "Toyota", "Corolla", 2023, 25000.0, 139, 5.8, "Gasoline", true, "FWD", "White"),
                new Car(2L, "BMW", "M3", 2023, 75000.0, 473, 12.5, "Gasoline", false, "RWD", "White"),
                new Car(3L, "Honda", "Civic", 2023, 26000.0, 158, 6.2, "Gasoline", true, "FWD", "Silver"),
                new Car(4L, "Tesla", "Model 3", 2023, 45000.0, 283, 0.0, "Electric", true, "RWD", "White")
        );

        // Create sample preferences
        samplePreferences = new UserPreferences(
                50000.0, // budget
                "intermediate", // experience
                "city", // useCase
                Arrays.asList("Toyota", "Honda"), // brandPreferences
                true // fuelEconomyPriority
        );
    }

    @Test
    void getRecommendations_ShouldFilterCarsByBudget() {
        // Given
        when(carRepository.findAll()).thenReturn(sampleCars);

        // When
        List<RecommendationResult> results = recommendationService.getRecommendations(samplePreferences);

        // Then
        assertNotNull(results);
        assertTrue(results.stream().allMatch(r -> r.getPrice() <= 50000.0));
        verify(carRepository).findAll();
    }

    @Test
    void getRecommendations_ShouldFilterCarsByExperience() {
        // Given
        UserPreferences novicePreferences = new UserPreferences(
                50000.0, "novice", "city", null, true
        );
        when(carRepository.findAll()).thenReturn(sampleCars);

        // When
        List<RecommendationResult> results = recommendationService.getRecommendations(novicePreferences);

        // Then
        assertNotNull(results);
        assertTrue(results.stream().allMatch(r -> r.getHorsePower() <= 150));
    }

    @Test
    void getRecommendations_ShouldFilterCarsByUseCase() {
        // Given
        when(carRepository.findAll()).thenReturn(sampleCars);

        // When
        List<RecommendationResult> results = recommendationService.getRecommendations(samplePreferences);

        // Then
        assertNotNull(results);
        assertTrue(results.stream().allMatch(r -> r.isCompact())); // City use requires compact cars
    }

    @Test
    void getRecommendations_ShouldFilterCarsByFuelEconomy() {
        // Given
        when(carRepository.findAll()).thenReturn(sampleCars);

        // When
        List<RecommendationResult> results = recommendationService.getRecommendations(samplePreferences);

        // Then
        assertNotNull(results);
        assertTrue(results.stream().allMatch(r -> r.getFuelConsumption() <= 7.0));
    }

    @Test
    void getRecommendations_ShouldFilterCarsByBrandPreferences() {
        // Given
        when(carRepository.findAll()).thenReturn(sampleCars);

        // When
        List<RecommendationResult> results = recommendationService.getRecommendations(samplePreferences);

        // Then
        assertNotNull(results);
        assertTrue(results.stream().allMatch(r -> 
                "Toyota".equals(r.getBrand()) || "Honda".equals(r.getBrand())));
    }

    @Test
    void getRecommendations_ShouldReturnTop5Results() {
        // Given
        when(carRepository.findAll()).thenReturn(sampleCars);

        // When
        List<RecommendationResult> results = recommendationService.getRecommendations(samplePreferences);

        // Then
        assertNotNull(results);
        assertTrue(results.size() <= 5);
    }

    @Test
    void getRecommendations_ShouldSortByScoreDescending() {
        // Given
        when(carRepository.findAll()).thenReturn(sampleCars);

        // When
        List<RecommendationResult> results = recommendationService.getRecommendations(samplePreferences);

        // Then
        assertNotNull(results);
        if (results.size() > 1) {
            for (int i = 0; i < results.size() - 1; i++) {
                assertTrue(results.get(i).getScore() >= results.get(i + 1).getScore());
            }
        }
    }

    @Test
    void getRecommendations_ShouldReturnEmptyListWhenNoCarsMatch() {
        // Given
        UserPreferences strictPreferences = new UserPreferences(
                10000.0, // Very low budget
                "novice",
                "city",
                Arrays.asList("Ferrari"), // Expensive brand
                true
        );
        when(carRepository.findAll()).thenReturn(sampleCars);

        // When
        List<RecommendationResult> results = recommendationService.getRecommendations(strictPreferences);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void getRecommendations_ShouldGenerateValidRecommendationResults() {
        // Given
        when(carRepository.findAll()).thenReturn(sampleCars);

        // When
        List<RecommendationResult> results = recommendationService.getRecommendations(samplePreferences);

        // Then
        assertNotNull(results);
        for (RecommendationResult result : results) {
            assertNotNull(result.getModel());
            assertNotNull(result.getReason());
            assertTrue(result.getScore() >= 0 && result.getScore() <= 100,
                    "Score should stay within the documented 0-100 scale, was " + result.getScore());
            assertNotNull(result.getBrand());
            assertTrue(result.getPrice() > 0);
            assertTrue(result.getYear() > 0);
            assertTrue(result.getHorsePower() > 0);
            assertTrue(result.getFuelConsumption() >= 0);
            assertNotNull(result.getFuelType());
            assertNotNull(result.getDrivetrainType());
            assertNotNull(result.getColor());
        }
    }
}
