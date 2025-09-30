package ai.carmatch.controller;

import ai.carmatch.dto.RecommendationResult;
import ai.carmatch.model.UserPreferences;
import ai.carmatch.service.RecommendationService;
import ai.carmatch.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow CORS for frontend integration
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    private final UserService userService;
    
    /**
     * Get car recommendations based on user's saved preferences
     * GET /api/recommend/my-preferences
     */
    @GetMapping("/recommend")
    public ResponseEntity<?> getRecommendationsFromUserPreferences(Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("Getting recommendations for user's saved preferences: {}", username);
            
            // Get user's preferences
            var userProfile = userService.getUserProfile(username);
            UserPreferences preferences = userProfile.getPreferences();
            
            if (preferences == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No preferences found. Please set your preferences first.");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<RecommendationResult> recommendations = recommendationService.getRecommendations(preferences);
            
            if (recommendations.isEmpty()) {
                log.warn("No cars found matching the user's preferences");
                return ResponseEntity.ok(recommendations);
            }
            
            log.info("Returning {} recommendations for user: {}", recommendations.size(), username);
            return ResponseEntity.ok(recommendations);
            
        } catch (IllegalArgumentException e) {
            log.warn("User not found: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error generating recommendations from user preferences", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate recommendations");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "CarMatchAI");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Get application info
     * GET /api/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "CarMatchAI");
        info.put("description", "AI-powered car recommendation service");
        info.put("version", "1.0.0");
        info.put("features", "Rule-based recommendation engine with Redis caching");
        
        return ResponseEntity.ok(info);
    }
}
