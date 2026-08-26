package ai.carmatch.service;

import ai.carmatch.dto.AiExplanationsResponse;
import ai.carmatch.dto.RecommendationResult;
import ai.carmatch.model.UserPreferences;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Rewrites the template-generated "reason" text on already-scored
 * recommendations into a natural-language explanation, without touching
 * which cars were picked or how they were scored - that stays in
 * RecommendationService's deterministic rule engine so tests, caching, and
 * the score itself never depend on what an LLM happens to say.
 * <p>
 * One chat call covers the whole batch (typically <=5 cars) instead of one
 * call per car, and only the facts already computed for each car (brand,
 * price, fuel consumption, score, ...) are sent to the model - it is asked to
 * phrase them, not to invent specs. Any failure (Ollama not running, a
 * malformed response, a mismatched list size) is caught and logged, and the
 * caller keeps the original template reasons; the feature is additive, never
 * load-bearing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiExplanationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Value("${carmatch.ai.explanations-enabled:true}")
    private boolean explanationsEnabled;

    private static final String SYSTEM_PROMPT = """
            You write a short, friendly one-sentence explanation for each
            recommended car, based ONLY on the facts given for that car and the
            buyer's stated preferences. Do not invent specs, features, or prices
            that are not present in the facts. Return exactly one explanation per
            car, in the same order the cars are given in.
            """;

    private record CarFacts(String brand, String model, double price, double fuelConsumption,
                             int horsePower, boolean compact, double score) {
    }

    public List<String> explain(List<RecommendationResult> recommendations, UserPreferences preferences) {
        if (!explanationsEnabled || recommendations.isEmpty()) {
            return null;
        }

        try {
            List<CarFacts> facts = recommendations.stream()
                    .map(r -> new CarFacts(r.getBrand(), r.getModel(), r.getPrice(), r.getFuelConsumption(),
                            r.getHorsePower(), r.isCompact(), r.getScore()))
                    .toList();

            String prompt = """
                    Buyer preferences: budget=%.2f, experience=%s, useCase=%s, fuelEconomyPriority=%s

                    Cars, in order: %s
                    """.formatted(preferences.getBudget(), preferences.getExperience(), preferences.getUseCase(),
                    preferences.getFuelEconomyPriority(), objectMapper.writeValueAsString(facts));

            AiExplanationsResponse response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(prompt)
                    .call()
                    .entity(AiExplanationsResponse.class);

            if (response == null || response.reasons() == null || response.reasons().size() != recommendations.size()) {
                log.warn("AI explanation response shape mismatch, keeping template reasons");
                return null;
            }

            return response.reasons();
        } catch (Exception e) {
            log.warn("AI explanation generation failed ({}), keeping template reasons", e.getMessage());
            return null;
        }
    }
}
