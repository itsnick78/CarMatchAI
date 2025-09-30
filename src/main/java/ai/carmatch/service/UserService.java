package ai.carmatch.service;

import ai.carmatch.dto.UserProfileResponse;
import ai.carmatch.dto.UserRegistrationRequest;
import ai.carmatch.dto.UserPreferencesUpdateRequest;
import ai.carmatch.model.User;
import ai.carmatch.model.UserPreferences;
import ai.carmatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Register a new user
     */
    @Transactional
    public UserProfileResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        
        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        
        return UserProfileResponse.fromUser(savedUser);
    }
    
    /**
     * Get user profile by username
     */
    public UserProfileResponse getUserProfile(String username) {
        log.info("Getting profile for user: {}", username);
        
        User user = userRepository.findByUsernameWithPreferences(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return UserProfileResponse.fromUser(user);
    }
    
    /**
     * Get user profile by ID
     */
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("Getting profile for user ID: {}", userId);
        
        User user = userRepository.findByIdWithPreferences(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return UserProfileResponse.fromUser(user);
    }
    
    /**
     * Update user preferences
     */
    @Transactional
    public UserProfileResponse updateUserPreferences(String username, UserPreferencesUpdateRequest request) {
        log.info("Updating preferences for user: {}", username);
        
        User user = userRepository.findByUsernameWithPreferences(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        UserPreferences preferences = user.getPreferences();
        if (preferences == null) {
            // Create new preferences if they don't exist
            preferences = new UserPreferences();
            preferences.setUser(user);
            user.setPreferences(preferences);
        }
        
        // Update preferences
        preferences.setBudget(request.getBudget());
        preferences.setExperience(request.getExperience());
        preferences.setUseCase(request.getUseCase());
        preferences.setBrandPreferences(request.getBrandPreferences());
        preferences.setFuelEconomyPriority(request.getFuelEconomyPriority());
        
        // Save user with updated preferences
        User savedUser = userRepository.save(user);
        log.info("Preferences updated successfully for user: {}", username);
        
        return UserProfileResponse.fromUser(savedUser);
    }
    
    /**
     * Update user basic information
     */
    @Transactional
    public UserProfileResponse updateUserProfile(String username, String firstName, String lastName, String email) {
        log.info("Updating profile for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        
        User savedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", username);
        
        return UserProfileResponse.fromUser(savedUser);
    }
    
    /**
     * Delete user account
     */
    @Transactional
    public void deleteUser(String username) {
        log.info("Deleting user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        userRepository.delete(user);
        log.info("User deleted successfully: {}", username);
    }
    
    /**
     * Check if user exists by username
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Get user by username for authentication
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Get user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Load user by username for Spring Security authentication
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
