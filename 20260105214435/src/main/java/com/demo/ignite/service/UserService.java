package com.demo.ignite.service;

import com.demo.ignite.entity.User;
import com.demo.ignite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User Service
 * Business logic layer for User operations
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Create or update a user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Delete user by ID
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Find users by username
     */
    public List<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find users by email
     */
    public List<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get active users
     */
    public List<User> getActiveUsers() {
        return userRepository.findActiveUsers();
    }

    /**
     * Find users by age range
     */
    public List<User> findByAgeRange(Integer minAge, Integer maxAge) {
        return userRepository.findByAgeRange(minAge, maxAge);
    }

    /**
     * Count total users
     */
    public Long countUsers() {
        return userRepository.count();
    }

    /**
     * Delete all users
     */
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}
