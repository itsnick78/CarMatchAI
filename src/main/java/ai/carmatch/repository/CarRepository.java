package ai.carmatch.repository;

import ai.carmatch.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByPriceLessThanEqualOrderByPriceAsc(BigDecimal price);

    List<Car> findByIsCompactTrue();

    List<Car> findByFuelConsumptionLessThanEqualOrderByFuelConsumptionAsc(BigDecimal fuelConsumption);

    List<Car> findByHorsePowerLessThanEqualOrderByHorsePowerAsc(BigDecimal horsePower);

    List<Car> findByBrandIn(List<String> brands);
}