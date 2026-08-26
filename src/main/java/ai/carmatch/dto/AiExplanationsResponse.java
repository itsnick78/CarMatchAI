package ai.carmatch.dto;

import java.util.List;

/**
 * Structured-output target for the batched explanation prompt in
 * AiExplanationService. Spring AI's ChatClient.entity(Class) builds a JSON
 * schema from this shape, appends it to the prompt as a format instruction,
 * and parses the model's JSON response back into it - one call covers every
 * recommended car instead of one LLM round trip per car.
 */
public record AiExplanationsResponse(List<String> reasons) {
}
