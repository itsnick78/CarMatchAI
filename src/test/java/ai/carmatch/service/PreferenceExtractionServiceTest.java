package ai.carmatch.service;

import ai.carmatch.dto.UserPreferencesUpdateRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * The ChatClient's fluent builder chain is mocked here rather than pointed at
 * a real Ollama instance: LLM output is non-deterministic, so pinning the
 * "model returned this JSON" case to a fixed mocked value is what makes the
 * validation/mapping logic in PreferenceExtractionService actually testable.
 * A real end-to-end call is verified manually against a live Ollama
 * container instead (see README).
 */
@ExtendWith(MockitoExtension.class)
class PreferenceExtractionServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private Validator validator;
    private PreferenceExtractionService service;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        service = new PreferenceExtractionService(chatClient, validator);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
    }

    @Test
    void extract_ShouldReturnPreferencesWhenLlmOutputIsValid() {
        UserPreferencesUpdateRequest llmOutput = new UserPreferencesUpdateRequest(
                40000.0, "intermediate", "city", Arrays.asList("Toyota", "Honda"), true);
        when(callResponseSpec.entity(UserPreferencesUpdateRequest.class)).thenReturn(llmOutput);

        UserPreferencesUpdateRequest result = service.extract("недорогая экономичная машина для города");

        assertEquals(40000.0, result.getBudget());
        assertEquals("intermediate", result.getExperience());
        assertEquals("city", result.getUseCase());
        assertEquals(List.of("Toyota", "Honda"), result.getBrandPreferences());
        assertEquals(true, result.getFuelEconomyPriority());
    }

    @Test
    void extract_ShouldThrowWhenModelReturnsNull() {
        when(callResponseSpec.entity(UserPreferencesUpdateRequest.class)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.extract("some text"));
    }

    @Test
    void extract_ShouldThrowWhenLlmOutputFailsValidation() {
        // "sedan" is not one of the allowed useCase values - simulates the
        // model not following the enum constraint from the system prompt.
        UserPreferencesUpdateRequest invalid = new UserPreferencesUpdateRequest(
                40000.0, "intermediate", "sedan", null, true);
        when(callResponseSpec.entity(UserPreferencesUpdateRequest.class)).thenReturn(invalid);

        assertThrows(IllegalArgumentException.class, () -> service.extract("some text"));
    }

    @Test
    void extract_ShouldThrowWhenBudgetOutOfAllowedRange() {
        UserPreferencesUpdateRequest tooExpensive = new UserPreferencesUpdateRequest(
                500000.0, "expert", "highway", null, false);
        when(callResponseSpec.entity(UserPreferencesUpdateRequest.class)).thenReturn(tooExpensive);

        assertThrows(IllegalArgumentException.class, () -> service.extract("very expensive car"));
    }
}
