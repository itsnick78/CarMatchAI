package ai.carmatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

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
    
    @ElementCollection
    @CollectionTable(name = "user_brand_preferences", joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "brand_name")
    private List<String> brandPreferences;
    
    @NotNull(message = "Fuel economy priority is required")
    private Boolean fuelEconomyPriority;
    
    @Override
    public String toString() {
        return String.format("UserPreferences{budget=%.2f, experience='%s', useCase='%s', brandPreferences=%s, fuelEconomyPriority=%s}", 
                budget, experience, useCase, brandPreferences, fuelEconomyPriority);
    }

    public UserPreferences(Double budget, String experience, String useCase, List<String> brandPreferences, Boolean fuelEconomyPriority) {
        this.budget = budget;
        this.experience = experience;
        this.useCase = useCase;
        this.brandPreferences = brandPreferences;
        this.fuelEconomyPriority = fuelEconomyPriority;
    }
}
