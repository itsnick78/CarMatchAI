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
    @Transactional
    public UserProfileResponse registerUser(UserRegistrationRequest userRegistrationRequest) {
        log.info("Registering user: " + userRegistrationRequest.getUsername());

        if(userRepository.existsByUsername(userRegistrationRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if(userRepository.existsByEmail(userRegistrationRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(userRegistrationRequest.getUsername());
        user.setEmail(userRegistrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRegistrationRequest.getPassword()));
        user.setFirstName(userRegistrationRequest.getFirstName());
        user.setLastName(userRegistrationRequest.getLastName());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return UserProfileResponse.fromUser(savedUser);
    }

    public UserProfileResponse getUserProfile(String username) {
        log.info("Getting profile for user: {}", username);

        User user = userRepository.findByUsernameWithPreferences(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return UserProfileResponse.fromUser(user);
    }

    public UserProfileResponse getUserProfile(Long userId) {
        log.info("Getting profile for user: {}", userId);

        User user = userRepository.findByIdWithPreferences(userId)
                .orElseThrow(() -> new UsernameNotFoundException(userId.toString()));

        return UserProfileResponse.fromUser(user);
    }
    @Transactional
    public UserProfileResponse updateUserPreferences(String username, UserPreferencesUpdateRequest userPreferencesUpdateRequest) {
        log.info("Updating preferences for user: {}", username);

        User user = userRepository.findByUsernameWithPreferences(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        UserPreferences preferences = user.getPreferences();
        if(preferences == null) {
            preferences = new UserPreferences();
            preferences.setUser(user);
            user.setPreferences(preferences);
        }

        preferences.setBudget(userPreferencesUpdateRequest.getBudget());
        preferences.setExperience(userPreferencesUpdateRequest.getExperience());
        preferences.setUseCase(userPreferencesUpdateRequest.getUseCase());
        preferences.setBrandPreferences(userPreferencesUpdateRequest.getBrandPreferences());
        preferences.setFuelEconomyPriority(userPreferencesUpdateRequest.getFuelEconomyPriority());

        User savedUser = userRepository.save(user);
        log.info("User preferences updated successfully with it's ID: {}", savedUser.getId());

        return UserProfileResponse.fromUser(savedUser);
    }
    @Transactional
    public UserProfileResponse updateUserProfile(String username, String firstName, String lastName, String email) {
        log.info("Updating profile for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        if(!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);

        User savedUser = userRepository.save(user);
        log.info("User preferences updated successfully with it's ID: {}", savedUser.getId());

        return UserProfileResponse.fromUser(savedUser);
    }
    @Transactional
    public void deleteUser(String username) {
        log.info("Deleting user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        userRepository.delete(user);
        log.info("User deleted successfully with it's ID: {}", user.getId());
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}