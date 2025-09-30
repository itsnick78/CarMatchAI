package ai.carmatch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "cars")
@NoArgsConstructor
@AllArgsConstructor
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false, name = "car_year")
    private int year;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false, name = "horse_power")
    private int horsePower;

    @Column(nullable = false, name = "fuel_consumption")
    private double fuelConsumption;

    @Column(nullable = false, name = "fuel_type")
    private String fuelType;

    @Column(nullable = false, name = "is_compact")
    private boolean isCompact;

    @Column(nullable = false, name = "drivetrain_type")
    private String drivetrainType;

    @Column(nullable = false)
    private String color;

    public Long getId() {
        return this.id;
    }

    public String getBrand() {
        return this.brand;
    }

    public String getModel() {
        return this.model;
    }

    public int getYear() {
        return this.year;
    }

    public double getPrice() {
        return this.price;
    }

    public int getHorsePower() {
        return this.horsePower;
    }

    public double getFuelConsumption() {
        return this.fuelConsumption;
    }

    public String getFuelType() {
        return this.fuelType;
    }

    public boolean isCompact() {
        return this.isCompact;
    }

    public String getDrivetrainType() {
        return this.drivetrainType;
    }

    public String getColor() {
        return this.color;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setHorsePower(int horsePower) {
        this.horsePower = horsePower;
    }

    public void setFuelConsumption(double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public void setCompact(boolean isCompact) {
        this.isCompact = isCompact;
    }

    public void setDrivetrainType(String drivetrainType) {
        this.drivetrainType = drivetrainType;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Car car)) return false;
        return year == car.year && Double.compare(price, car.price) == 0 && horsePower == car.horsePower && Double.compare(fuelConsumption, car.fuelConsumption) == 0 && isCompact == car.isCompact && Objects.equals(id, car.id) && Objects.equals(brand, car.brand) && Objects.equals(model, car.model) && Objects.equals(fuelType, car.fuelType) && Objects.equals(drivetrainType, car.drivetrainType) && Objects.equals(color, car.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, brand, model, year, price, horsePower, fuelConsumption, fuelType, isCompact, drivetrainType, color);
    }

    public String toString() {
        return "Car(id=" + this.getId() + ", brand=" + this.getBrand() + ", model=" + this.getModel() + ", year=" + this.getYear() + ", price=" + this.getPrice() + ", horsePower=" + this.getHorsePower() + ", fuelConsumption=" + this.getFuelConsumption() + ", fuelType=" + this.getFuelType() + ", isCompact=" + this.isCompact() + ", drivetrainType=" + this.getDrivetrainType() + ", color=" + this.getColor() + ")";
    }
}
