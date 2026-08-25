package ai.carmatch.dto;

import lombok.Getter;
import lombok.Setter;


public class CarUpdateRequest {
    private String brand;
    private String model;
    private Integer year;
    private Double price;
    private Integer horsePower;
    private Double fuelConsumption;
    private String fuelType;
    private Boolean compact;
    private String drivetrainType;
    private String color;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getHorsePower() {
        return horsePower;
    }

    public void setHorsePower(Integer horsePower) {
        this.horsePower = horsePower;
    }

    public Double getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(Double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public Boolean isCompact() {
        return compact;
    }

    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    public String getDrivetrainType() {
        return drivetrainType;
    }

    public void setDrivetrainType(String drivetrainType) {
        this.drivetrainType = drivetrainType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
