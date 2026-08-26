package ai.carmatch.service;

import ai.carmatch.dto.UserPreferencesUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Turns a free-text description of what the user wants ("нужна недорогая
 * компактная машина для города, экономичная") into the same
 * UserPreferencesUpdateRequest the manual preferences form produces.
 * <p>
 * The extraction target is deliberately the existing request DTO, not a new
 * AI-only model: it already carries the Bean Validation rules that define a
 * valid preference set (enum patterns for experience/useCase, budget range),
 * so the LLM's output is checked against the exact same rules a hand-filled
 * form would be, and both paths end up calling
 * {@link UserService#updateUserPreferences} - there is only one place that
 * writes preferences to the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PreferenceExtractionService {

    private final ChatClient chatClient;
    private final Validator validator;

    private static final String SYSTEM_PROMPT = """
            You extract structured car-shopping preferences from a user's free-text
            description, which may be written in any language (including Russian).
            Rules for the fields you output:
            - "experience" must be exactly one of: novice, intermediate, expert.
            - "useCase" must be exactly one of: city, highway, mixed, offroad.
            - "budget" is a number in US dollars. If the user gives a range, use the
              upper bound. If no currency is mentioned, assume USD. If no budget is
              mentioned at all, use 30000.
            - "brandPreferences" is a list of car brand names mentioned by the user,
              or an empty list if none are mentioned.
            - "fuelEconomyPriority" is true if the user cares about fuel efficiency,
              economy, or low consumption, false otherwise.
            Make the most reasonable assumption for any field that cannot be
            directly inferred rather than leaving it out.
            """;

    public UserPreferencesUpdateRequest extract(String freeText) {
        UserPreferencesUpdateRequest extracted = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(freeText)
                .call()
                .entity(UserPreferencesUpdateRequest.class);

        if (extracted == null) {
            throw new IllegalArgumentException("Could not extract preferences from the provided text");
        }

        Set<ConstraintViolation<UserPreferencesUpdateRequest>> violations = validator.validate(extracted);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            log.warn("LLM-extracted preferences failed validation: {}", message);
            throw new IllegalArgumentException("Extracted preferences are invalid: " + message);
        }

        log.info("Extracted preferences from text: {}", extracted);
        return extracted;
    }
}
