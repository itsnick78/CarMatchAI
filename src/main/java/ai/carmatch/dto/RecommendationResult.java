package ai.carmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private String model;
    private String reason;
    private double score;
    private String brand;
    private double price;
    private int year;
    private int horsePower;
    private double fuelConsumption;
    private String fuelType;
    private boolean isCompact;
    private String drivetrainType;
    private String color;
}