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

    public List<RecommendationResult> getRecommendations(UserPreferences userPreferences) {
        log.info("Generating recommendations: ");

        List<Car> allCars = carRepository.findAll();
        List<Car> filteredCars = applyFilters(allCars, userPreferences);

        List<RecommendationResult> recommendations = filteredCars.stream()
                .map(car -> createRecommendationResult(car, userPreferences))
                .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .limit(5)
                .collect(Collectors.toList());

        log.info("Recommendations: {}", recommendations.size());
        return recommendations;
    }

    private List<Car> applyFilters(List<Car> cars, UserPreferences userPreferences) {
        return cars.stream()
                .filter(car -> car.getPrice() <= userPreferences.getBudget())
                .filter(car -> {
                    if("novice".equalsIgnoreCase(userPreferences.getExperience())) {
                        return car.getHorsePower() <= 150;
                    }
                    return true;
                })
                .filter(car -> {
                    if("city".equalsIgnoreCase(userPreferences.getUseCase())) {
                        return car.isCompact();
                    }
                    return true;
                })
                .filter(car -> {
                    if(userPreferences.getFuelEconomyPriority()) {
                        return car.getFuelConsumption() <= 7.0;
                    }
                    return true;
                })
                .filter(car -> {
                    if(userPreferences.getBrandPreferences() != null && !userPreferences.getBrandPreferences().isEmpty()) {
                        return userPreferences.getBrandPreferences().contains(car.getBrand());
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private RecommendationResult createRecommendationResult(Car car, UserPreferences userPreferences) {
        double score = calculateScore(car, userPreferences);
        String reason = generateReason(car, userPreferences);

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

    private double calculateScore(Car car, UserPreferences userPreferences) {
        double score = 0.0;

        double priceRatio = car.getPrice() / userPreferences.getBudget();
        score += (1.0 - priceRatio) * 40;

        if(userPreferences.getFuelEconomyPriority()) {
            double fuelScore = Math.max(0, (10.0 - car.getFuelConsumption()) * 10.0 * 30);
            score += fuelScore;
        } else {
            double fuelScore = Math.max(0, (15.0 - car.getFuelConsumption()) / 15.0 * 15);
            score += fuelScore;
        }

        if("novice".equalsIgnoreCase(userPreferences.getExperience())) {
            if(car.getHorsePower() <= 100) {
                score += 20;
            } else if(car.getHorsePower() <= 150) {
                score += 10;
            }
        } else {
            if(car.getHorsePower() >= 200) {
                score += 20;
            } else if(car.getHorsePower() >= 150) {
                score += 15;
            } else {
                score += 5;
            }
        }

        if("city".equalsIgnoreCase(userPreferences.getUseCase()) && car.isCompact()) {
            score += 10;
        } else if("highway".equalsIgnoreCase(userPreferences.getUseCase()) && car.getHorsePower() >= 150) {
            score += 10;
        } else if("mixed".equalsIgnoreCase(userPreferences.getUseCase())) {
            score += 5;
        }

        return Math.max(0.0, score);
    }

    private String generateReason(Car car, UserPreferences userPreferences) {
        List<String> reasons = new ArrayList<>();

        double priceRatio = (car.getPrice() / userPreferences.getBudget()) * 100;
        if (priceRatio < 50) {
            reasons.add("excellent value for money");
        } else if (priceRatio < 80) {
            reasons.add("good value within budget");
        } else {
            reasons.add("fits your budget");
        }

        // Fuel economy reason
        if (userPreferences.getFuelEconomyPriority() && car.getFuelConsumption() <= 6.0) {
            reasons.add("excellent fuel economy");
        } else if (car.getFuelConsumption() <= 8.0) {
            reasons.add("good fuel efficiency");
        }

        // Experience reason
        if ("novice".equals(userPreferences.getExperience()) && car.getHorsePower() <= 120) {
            reasons.add("perfect for new drivers");
        } else if ("expert".equals(userPreferences.getExperience()) && car.getHorsePower() >= 200) {
            reasons.add("powerful engine for experienced drivers");
        }

        // Use case reason
        if ("city".equals(userPreferences.getUseCase()) && car.isCompact()) {
            reasons.add("compact size ideal for city driving");
        } else if ("highway".equals(userPreferences.getUseCase()) && car.getHorsePower() >= 150) {
            reasons.add("strong performance for highway driving");
        }

        // Brand preference reason
        if (userPreferences.getBrandPreferences() != null && userPreferences.getBrandPreferences().contains(car.getBrand())) {
            reasons.add("matches your preferred brand");
        }

        if (reasons.isEmpty()) {
            reasons.add("meets your basic requirements");
        }

        return String.join(", ", reasons);
    }
}