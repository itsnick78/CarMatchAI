package ai.carmatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NaturalLanguagePreferencesRequest {

    @NotBlank(message = "Text is required")
    @Size(max = 2000, message = "Text must not exceed 2000 characters")
    private String text;
}
