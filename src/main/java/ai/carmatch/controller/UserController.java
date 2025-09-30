package ai.carmatch.controller;

import ai.carmatch.dto.UserLoginRequest;
import ai.carmatch.dto.UserProfileResponse;
import ai.carmatch.dto.UserRegistrationRequest;
import ai.carmatch.dto.UserPreferencesUpdateRequest;
import ai.carmatch.model.User;
import ai.carmatch.service.UserService;
import ai.carmatch.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    
    /**
     * Register a new user
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            log.info("Received registration request for username: {}", request.getUsername());
            
            UserProfileResponse userProfile = userService.registerUser(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("user", userProfile);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            log.error("Error during user registration", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            log.info("Login attempt for email: {}", request.getEmail());

            // Load user by email, but Spring Security authenticates by username
            User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), request.getPassword());
            authenticationManager.authenticate(authToken);

            UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            String jwt = jwtService.generateToken(userDetails);

            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("AUTH_TOKEN", jwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(jwtService.getExpirationSeconds());
            // For dev over HTTP you may want this false; set to true when using HTTPS
            cookie.setSecure(false);

            org.springframework.http.ResponseCookie responseCookie = org.springframework.http.ResponseCookie
                    .from("AUTH_TOKEN", jwt)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(jwtService.getExpirationSeconds())
                    .sameSite("Lax")
                    .build();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");

            return ResponseEntity.ok()
                    .header("Set-Cookie", responseCookie.toString())
                    .body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            log.error("Error during login", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Login failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get current user profile
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("Getting profile for user: {}", username);
            
            UserProfileResponse profile = userService.getUserProfile(username);
            return ResponseEntity.ok(profile);
            
        } catch (IllegalArgumentException e) {
            log.warn("Profile not found: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error getting user profile", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get user preferences
     * GET /api/users/preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<?> getUserPreferences(Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("Getting preferences for user: {}", username);
            
            UserProfileResponse profile = userService.getUserProfile(username);
            
            if (profile.getPreferences() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No preferences found. Please set your preferences first.");
                return ResponseEntity.badRequest().body(error);
            }
            
            return ResponseEntity.ok(profile.getPreferences());
            
        } catch (IllegalArgumentException e) {
            log.warn("Preferences not found: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error getting user preferences", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve preferences");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Create user preferences
     * POST /api/users/preferences
     */
    @PostMapping("/preferences")
    public ResponseEntity<?> createUserPreferences(
            @Valid @RequestBody UserPreferencesUpdateRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("Creating preferences for user: {}", username);
            
            UserProfileResponse profile = userService.updateUserPreferences(username, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Preferences created successfully");
            response.put("preferences", profile.getPreferences());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Preferences creation failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            log.error("Error creating user preferences", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create preferences");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Update user preferences
     * PUT /api/users/preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<?> updateUserPreferences(
            @Valid @RequestBody UserPreferencesUpdateRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("Updating preferences for user: {}", username);
            
            UserProfileResponse profile = userService.updateUserPreferences(username, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Preferences updated successfully");
            response.put("user", profile);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Preferences update failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            log.error("Error updating user preferences", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update preferences");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Update user basic profile information
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("Updating profile for user: {}", username);
            
            UserProfileResponse profile = userService.updateUserProfile(username, firstName, lastName, email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("user", profile);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Profile update failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            log.error("Error updating user profile", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Delete user account
     * DELETE /api/users/account
     */
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteUserAccount(Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("Deleting account for user: {}", username);
            
            userService.deleteUser(username);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Account deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Account deletion failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error deleting user account", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete account");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Check if username is available
     * GET /api/users/check-username?username={username}
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsernameAvailability(@RequestParam String username) {
        try {
            boolean available = !userService.userExists(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("available", available);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking username availability", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to check username availability");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
