package ai.carmatch.repository;

import ai.carmatch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find user with preferences by username
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.preferences WHERE u.username = :username")
    Optional<User> findByUsernameWithPreferences(@Param("username") String username);
    
    /**
     * Find user with preferences by ID
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.preferences WHERE u.id = :id")
    Optional<User> findByIdWithPreferences(@Param("id") Long id);
}





