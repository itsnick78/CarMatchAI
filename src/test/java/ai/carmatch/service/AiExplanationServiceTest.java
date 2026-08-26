package ai.carmatch.service;

import ai.carmatch.dto.AiExplanationsResponse;
import ai.carmatch.dto.RecommendationResult;
import ai.carmatch.model.UserPreferences;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiExplanationServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private AiExplanationService service;
    private List<RecommendationResult> recommendations;
    private UserPreferences preferences;

    @BeforeEach
    void setUp() {
        service = new AiExplanationService(chatClient, new ObjectMapper());
        ReflectionTestUtils.setField(service, "explanationsEnabled", true);

        recommendations = Arrays.asList(
                new RecommendationResult("Corolla", "template reason", 85.5, "Toyota", 25000.0, 2023,
                        139, 5.8, "Gasoline", true, "FWD", "White"),
                new RecommendationResult("Civic", "template reason", 80.0, "Honda", 26000.0, 2023,
                        158, 6.2, "Gasoline", true, "FWD", "Silver")
        );
        preferences = new UserPreferences(50000.0, "intermediate", "city", Arrays.asList("Toyota"), true);
    }

    private void stubChatClient() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
    }

    @Test
    void explain_ShouldReturnReasonsWhenModelRespondsWithMatchingSize() {
        stubChatClient();
        when(callResponseSpec.entity(AiExplanationsResponse.class))
                .thenReturn(new AiExplanationsResponse(List.of("great city car", "reliable and efficient")));

        List<String> result = service.explain(recommendations, preferences);

        assertEquals(List.of("great city car", "reliable and efficient"), result);
    }

    @Test
    void explain_ShouldReturnNullWhenResponseSizeMismatches() {
        stubChatClient();
        when(callResponseSpec.entity(AiExplanationsResponse.class))
                .thenReturn(new AiExplanationsResponse(List.of("only one reason")));

        List<String> result = service.explain(recommendations, preferences);

        assertNull(result);
    }

    @Test
    void explain_ShouldReturnNullWhenModelThrows() {
        stubChatClient();
        when(callResponseSpec.entity(AiExplanationsResponse.class))
                .thenThrow(new RuntimeException("Ollama unreachable"));

        List<String> result = service.explain(recommendations, preferences);

        assertNull(result);
    }

    @Test
    void explain_ShouldReturnNullWhenDisabled() {
        ReflectionTestUtils.setField(service, "explanationsEnabled", false);

        List<String> result = service.explain(recommendations, preferences);

        assertNull(result);
    }

    @Test
    void explain_ShouldReturnNullWhenNoRecommendations() {
        List<String> result = service.explain(List.of(), preferences);

        assertNull(result);
    }
}
