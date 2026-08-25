package ai.carmatch.controller;

import ai.carmatch.dto.CarUpdateRequest;
import ai.carmatch.model.Car;
import ai.carmatch.repository.CarRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarRepository carRepository;

    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @GetMapping
    public ResponseEntity<List<Car>> getAllCars() {
        List<Car> cars = carRepository.findAll();
        return ResponseEntity.ok(cars);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        return carRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    @CacheEvict(cacheNames = "recommendations", allEntries = true)
    public ResponseEntity<Car> createCar(@RequestBody Car car) {
        Car saved = carRepository.save(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/edit/{id}")
    @CacheEvict(cacheNames = "recommendations", allEntries = true)
    public ResponseEntity<Car> updateCar(@PathVariable Long id, @RequestBody CarUpdateRequest update) {
        return carRepository.findById(id)
                .map(existing -> {
                    existing.setBrand(update.getBrand());
                    existing.setModel(update.getModel());
                    existing.setYear(update.getYear());
                    existing.setPrice(update.getPrice());
                    existing.setHorsePower(update.getHorsePower());
                    existing.setFuelConsumption(update.getFuelConsumption());
                    existing.setFuelType(update.getFuelType());
                    existing.setCompact(update.isCompact());
                    existing.setDrivetrainType(update.getDrivetrainType());
                    existing.setColor(update.getColor());
                    Car saved = carRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @CacheEvict(cacheNames = "recommendations", allEntries = true)
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        if (!carRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        carRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}


