package ai.carmatch.service;

import ai.carmatch.model.Car;
import ai.carmatch.dto.RecommendationResult;
import ai.carmatch.model.UserPreferences;
import ai.carmatch.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
    
    private final CarRepository carRepository;
    
    /**
     * Get car recommendations based on user preferences
     * Results are cached in Redis using the user preferences as the key
     */
    @Cacheable(value = "recommendations", key = "#prefs.toString()")
    public List<RecommendationResult> getRecommendations(UserPreferences prefs) {
        log.info("Generating recommendations for preferences: {}", prefs);
        
        // Get all cars and apply filters
        List<Car> allCars = carRepository.findAll();
        List<Car> filteredCars = applyFilters(allCars, prefs);
        
        // Calculate scores and generate recommendations
        List<RecommendationResult> recommendations = filteredCars.stream()
                .map(car -> createRecommendationResult(car, prefs))
                .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore())) // Sort by score descending
                .limit(5) // Return top 5
                .collect(Collectors.toList());
        
        log.info("Generated {} recommendations", recommendations.size());
        return recommendations;
    }
    
    /**
     * Apply filtering rules based on user preferences
     */
    private List<Car> applyFilters(List<Car> cars, UserPreferences prefs) {
        return cars.stream()
                .filter(car -> car.getPrice() <= prefs.getBudget()) // Budget filter
                .filter(car -> {
                    // Experience filter: novice drivers get limited horsepower
                    if ("novice".equals(prefs.getExperience())) {
                        return car.getHorsePower() <= 150;
                    }
                    return true;
                })
                .filter(car -> {
                    // Use case filter: city use requires compact cars
                    if ("city".equals(prefs.getUseCase())) {
                        return car.isCompact();
                    }
                    return true;
                })
                .filter(car -> {
                    // Fuel economy filter
                    if (prefs.getFuelEconomyPriority()) {
                        return car.getFuelConsumption() <= 7.0;
                    }
                    return true;
                })
                .filter(car -> {
                    // Brand preferences filter (if specified)
                    if (prefs.getBrandPreferences() != null && !prefs.getBrandPreferences().isEmpty()) {
                        return prefs.getBrandPreferences().contains(car.getBrand());
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Create a recommendation result with score calculation
     */
    private RecommendationResult createRecommendationResult(Car car, UserPreferences prefs) {
        double score = calculateScore(car, prefs);
        String reason = generateReason(car, prefs);
        
        return new RecommendationResult(
                car.getModel(),
                reason,
                score,
                car.getBrand(),
                car.getPrice(),
                car.getYear(),
                car.getHorsePower(),
                car.getFuelConsumption(),
                car.getFuelType(),
                car.isCompact(),
                car.getDrivetrainType(),
                car.getColor()
        );
    }
    
    /**
     * Calculate recommendation score (on a 0–100 scale (0 = worst, 100 = best))
     * Scoring factors:
     * - Price efficiency (lower price relative to budget = higher score)
     * - Fuel economy (lower consumption = higher score)
     * - Horsepower appropriateness for experience level
     * - Use case suitability
     */
    private double calculateScore(Car car, UserPreferences prefs) {
        double score = 0.0;
        
        // Price efficiency score (0-40 points)
        double priceRatio = car.getPrice() / prefs.getBudget();
        score += (1.0 - priceRatio) * 40;
        
        // Fuel economy score (0-30 points)
        if (prefs.getFuelEconomyPriority()) {
            double fuelScore = Math.max(0, (10.0 - car.getFuelConsumption()) / 10.0 * 30);
            score += fuelScore;
        } else {
            // Still consider fuel economy but with lower weight
            double fuelScore = Math.max(0, (15.0 - car.getFuelConsumption()) / 15.0 * 15);
            score += fuelScore;
        }
        
        // Experience appropriateness score (0-20 points)
        if ("novice".equals(prefs.getExperience())) {
            if (car.getHorsePower() <= 100) {
                score += 20;
            } else if (car.getHorsePower() <= 150) {
                score += 10;
            }
        } else if ("intermediate".equals(prefs.getExperience())) {
            if (car.getHorsePower() >= 100 && car.getHorsePower() <= 250) {
                score += 20;
            } else {
                score += 10;
            }
        } else { // expert
            if (car.getHorsePower() >= 200) {
                score += 20;
            } else if (car.getHorsePower() >= 150) {
                score += 15;
            } else {
                score += 5;
            }
        }
        
        // Use case suitability score (0-10 points)
        if ("city".equals(prefs.getUseCase()) && car.isCompact()) {
            score += 10;
        } else if ("highway".equals(prefs.getUseCase()) && car.getHorsePower() >= 150) {
            score += 10;
        } else if ("mixed".equals(prefs.getUseCase())) {
            score += 5; // Neutral score for mixed use
        }
        
        return Math.max(0, score); // Ensure non-negative score
    }
    
    /**
     * Generate human-readable reason for the recommendation
     */
    private String generateReason(Car car, UserPreferences prefs) {
        List<String> reasons = new ArrayList<>();
        
        // Price reason
        double priceRatio = (car.getPrice() / prefs.getBudget()) * 100;
        if (priceRatio < 50) {
            reasons.add("excellent value for money");
        } else if (priceRatio < 80) {
            reasons.add("good value within budget");
        } else {
            reasons.add("fits your budget");
        }
        
        // Fuel economy reason
        if (prefs.getFuelEconomyPriority() && car.getFuelConsumption() <= 6.0) {
            reasons.add("excellent fuel economy");
        } else if (car.getFuelConsumption() <= 8.0) {
            reasons.add("good fuel efficiency");
        }
        
        // Experience reason
        if ("novice".equals(prefs.getExperience()) && car.getHorsePower() <= 120) {
            reasons.add("perfect for new drivers");
        } else if ("expert".equals(prefs.getExperience()) && car.getHorsePower() >= 200) {
            reasons.add("powerful engine for experienced drivers");
        }
        
        // Use case reason
        if ("city".equals(prefs.getUseCase()) && car.isCompact()) {
            reasons.add("compact size ideal for city driving");
        } else if ("highway".equals(prefs.getUseCase()) && car.getHorsePower() >= 150) {
            reasons.add("strong performance for highway driving");
        }
        
        // Brand preference reason
        if (prefs.getBrandPreferences() != null && prefs.getBrandPreferences().contains(car.getBrand())) {
            reasons.add("matches your preferred brand");
        }
        
        if (reasons.isEmpty()) {
            reasons.add("meets your basic requirements");
        }
        
        return String.join(", ", reasons);
    }
}
