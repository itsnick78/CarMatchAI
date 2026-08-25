package ai.carmatch.service;

import ai.carmatch.model.Car;
import ai.carmatch.model.UserPreferences;
import ai.carmatch.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Exercises the real @Cacheable proxy (unlike RecommendationServiceTest,
 * which uses a plain Mockito @InjectMocks instance that Spring's AOP never
 * wraps). The test profile backs the cache with a simple in-memory
 * ConcurrentMapCacheManager instead of Redis - the caching *behavior* is
 * identical, so this needs no running Redis instance.
 */
@SpringBootTest
@ActiveProfiles("test")
class RecommendationServiceCachingTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private CarRepository carRepository;

    @BeforeEach
    void setUp() {
        List<Car> cars = Arrays.asList(
                new Car(1L, "Toyota", "Corolla", 2023, 25000.0, 139, 5.8, "Gasoline", true, "FWD", "White")
        );
        when(carRepository.findAll()).thenReturn(cars);
        cacheManager.getCache("recommendations").clear();
    }

    @Test
    void getRecommendations_ShouldHitCacheOnRepeatedCallWithEquivalentPreferences() {
        UserPreferences first = new UserPreferences(50000.0, "intermediate", "city", Arrays.asList("Toyota"), true);
        UserPreferences second = new UserPreferences(50000.0, "intermediate", "city", Arrays.asList("Toyota"), true);

        assertNotNull(recommendationService.getRecommendations(first));
        assertNotNull(recommendationService.getRecommendations(second));

        verify(carRepository, times(1)).findAll();
    }

    @Test
    void getRecommendations_ShouldRecomputeWhenPreferencesDiffer() {
        UserPreferences cheap = new UserPreferences(15000.0, "novice", "city", null, true);
        UserPreferences expensive = new UserPreferences(90000.0, "expert", "highway", null, false);

        recommendationService.getRecommendations(cheap);
        recommendationService.getRecommendations(expensive);

        verify(carRepository, times(2)).findAll();
    }
}
