package ai.carmatch.controller;

import ai.carmatch.dto.UserLoginRequest;
import ai.carmatch.dto.UserPreferencesUpdateRequest;
import ai.carmatch.dto.UserProfileResponse;
import ai.carmatch.dto.UserRegistrationRequest;
import ai.carmatch.model.User;
import ai.carmatch.security.JwtService;
import ai.carmatch.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {
        try {
            log.info("Received registration request for username: {}", userRegistrationRequest.getUsername());

            UserProfileResponse userProfileResponse = userService.registerUser(userRegistrationRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("user", userProfileResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            log.error("Error during user registration", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        try {
            log.info("Login attempt for email: {}", userLoginRequest.getEmail());

            User user = userService.findByEmail(userLoginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException(userLoginRequest.getEmail()));

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), userLoginRequest.getPassword());
            authenticationManager.authenticate(authToken);

            UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            String jwt = jwtService.generateToken(userDetails);
            Cookie cookie = new Cookie("AUTH_TOKEN", jwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(jwtService.getExpirationSeconds());
            // TODO : Set to true when using HTTPS
            cookie.setSecure(false);

            ResponseCookie responseCookie = ResponseCookie
                    .from("AUTH_TOKEN", jwt)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(jwtService.getExpirationSeconds())
                    .sameSite("Lax")
                    .build();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");

            return ResponseEntity.status(HttpStatus.OK)
                    .header("Set-Cookie", responseCookie.toString())
                    .body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            log.error("Error during login", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(Authentication auth) {
        try {
            String username = auth.getName();
            log.info("Retrieving user profile for username: {}", username);
            UserProfileResponse profile = userService.getUserProfile(username);
            return ResponseEntity.status(HttpStatus.OK).body(profile);
        } catch (IllegalArgumentException e) {
            log.warn("Profile not found: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            log.error("Error during user profile", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/preferences")
    public ResponseEntity<?> getUserPreferences(Authentication auth) {
        try {
            String username = auth.getName();
            log.info("Retrieving user preferences for username: {}", username);

            UserProfileResponse profile = userService.getUserProfile(username);

            if (profile.getPreferences() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No preferences found for username: {}" + username);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(error);
            }
            return ResponseEntity.status(HttpStatus.OK).body(profile.getPreferences());
        } catch (IllegalArgumentException e) {
            log.warn("Preferences not found: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            log.error("Error during user preferences", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/preferences")
    public ResponseEntity<?> createUserPreferences(
            @Valid @RequestBody UserPreferencesUpdateRequest request,
            Authentication auth) {
        try {
            String username = auth.getName();
            log.info("Creating user preferences for username: {}", username);
            UserProfileResponse profile = userService.updateUserPreferences(username, request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Preferences created successfully");
            response.put("preferences", profile.getPreferences());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Preferences creation failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            log.error("Error during creating user preferences", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

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