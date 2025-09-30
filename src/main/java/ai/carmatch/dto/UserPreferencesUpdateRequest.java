package ai.carmatch.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesUpdateRequest {
    
    @NotNull(message = "Budget is required")
    @Min(value = 1000, message = "Budget must be at least $1,000")
    @Max(value = 200000, message = "Budget must not exceed $200,000")
    private Double budget;
    
    @NotNull(message = "Experience level is required")
    @Pattern(regexp = "novice|intermediate|expert", message = "Experience must be novice, intermediate, or expert")
    private String experience;
    
    @NotNull(message = "Use case is required")
    @Pattern(regexp = "city|highway|mixed|offroad", message = "Use case must be city, highway, mixed, or offroad")
    private String useCase;
    
    private List<String> brandPreferences;
    
    @NotNull(message = "Fuel economy priority is required")
    private Boolean fuelEconomyPriority;
}





