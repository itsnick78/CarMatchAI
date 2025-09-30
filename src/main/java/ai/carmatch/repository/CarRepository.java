package ai.carmatch.repository;

import ai.carmatch.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    
    /**
     * Find cars within budget
     */
    List<Car> findByPriceLessThanEqualOrderByPriceAsc(Double maxPrice);
    
    /**
     * Find compact cars for city use
     */
    List<Car> findByIsCompactTrue();
    
    /**
     * Find cars with low fuel consumption
     */
    List<Car> findByFuelConsumptionLessThanEqualOrderByFuelConsumptionAsc(Double maxFuelConsumption);
    
    /**
     * Find cars with limited horsepower for novice drivers
     */
    List<Car> findByHorsePowerLessThanEqualOrderByHorsePowerAsc(Integer maxHorsePower);
    
    /**
     * Find cars by brand preferences
     */
    List<Car> findByBrandIn(List<String> brands);
}
