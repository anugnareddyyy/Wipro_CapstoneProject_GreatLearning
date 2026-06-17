package com.appverse.repository;

import com.appverse.entity.User;
import com.appverse.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity CRUD and query operations.
 * Extends JpaRepository to inherit standard persistence methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address (used during JWT authentication).
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by their username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user with the given email already exists.
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user with the given username already exists.
     */
    boolean existsByUsername(String username);

    /**
     * Retrieve all users with a specific role (e.g., find all developers).
     */
    List<User> findByRole(Role role);

    /**
     * Count total active users for admin dashboard metrics.
     */
    long countByIsActive(Boolean isActive);

    /**
     * Search users by username or email for admin user management.
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchUsers(@Param("query") String query);
}
