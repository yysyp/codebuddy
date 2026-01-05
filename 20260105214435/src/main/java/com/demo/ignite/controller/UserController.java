package com.demo.ignite.controller;

import com.demo.ignite.entity.User;
import com.demo.ignite.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User REST Controller
 * Provides REST endpoints for User CRUD operations and queries
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Create or update a user
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (user.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        User savedUser = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * Get all users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Delete user by ID
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Find users by username
     * GET /api/users/search/username/{username}
     */
    @GetMapping("/search/username/{username}")
    public ResponseEntity<List<User>> findByUsername(@PathVariable String username) {
        List<User> users = userService.findByUsername(username);
        return ResponseEntity.ok(users);
    }

    /**
     * Find users by email
     * GET /api/users/search/email/{email}
     */
    @GetMapping("/search/email/{email}")
    public ResponseEntity<List<User>> findByEmail(@PathVariable String email) {
        List<User> users = userService.findByEmail(email);
        return ResponseEntity.ok(users);
    }

    /**
     * Get active users
     * GET /api/users/search/active
     */
    @GetMapping("/search/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Find users by age range
     * GET /api/users/search/age-range?minAge=20&maxAge=40
     */
    @GetMapping("/search/age-range")
    public ResponseEntity<List<User>> findByAgeRange(
            @RequestParam Integer minAge,
            @RequestParam Integer maxAge) {
        List<User> users = userService.findByAgeRange(minAge, maxAge);
        return ResponseEntity.ok(users);
    }

    /**
     * Count total users
     * GET /api/users/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        Long count = userService.countUsers();
        return ResponseEntity.ok(count);
    }

    /**
     * Delete all users
     * DELETE /api/users
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllUsers() {
        userService.deleteAllUsers();
        return ResponseEntity.noContent().build();
    }
}
