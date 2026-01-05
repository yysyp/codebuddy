package com.demo.ignite.repository;

import com.demo.ignite.entity.User;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * User Repository for Ignite Cache Operations
 * Provides CRUD operations and queries on User entities
 */
@Repository
public class UserRepository {

    private static final String CACHE_NAME = "UserCache";

    @Autowired
    private Ignite ignite;

    /**
     * Get or create User Cache
     */
    private IgniteCache<Long, User> getUserCache() {
        return ignite.getOrCreateCache(CACHE_NAME);
    }

    /**
     * Save or update a user
     */
    public User save(User user) {
        IgniteCache<Long, User> cache = getUserCache();
        cache.put(user.getId(), user);
        return user;
    }

    /**
     * Find user by ID
     */
    public User findById(Long id) {
        IgniteCache<Long, User> cache = getUserCache();
        return cache.get(id);
    }

    /**
     * Find all users
     */
    public List<User> findAll() {
        IgniteCache<Long, User> cache = getUserCache();
        List<User> users = new ArrayList<>();
        cache.forEach(entry -> users.add(entry.getValue()));
        return users;
    }

    /**
     * Delete user by ID
     */
    public void deleteById(Long id) {
        IgniteCache<Long, User> cache = getUserCache();
        cache.remove(id);
    }

    /**
     * Find users by username
     */
    public List<User> findByUsername(String username) {
        IgniteCache<Long, User> cache = getUserCache();
        ScanQuery<Long, User> query = new ScanQuery<>();
        query.setFilter((key, user) -> username.equals(user.getUsername()));
        return cache.query(query).getAll().stream()
                .map(Cache.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Find users by email
     */
    public List<User> findByEmail(String email) {
        IgniteCache<Long, User> cache = getUserCache();
        ScanQuery<Long, User> query = new ScanQuery<>();
        query.setFilter((key, user) -> email.equals(user.getEmail()));
        return cache.query(query).getAll().stream()
                .map(Cache.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Find active users
     */
    public List<User> findActiveUsers() {
        IgniteCache<Long, User> cache = getUserCache();
        ScanQuery<Long, User> query = new ScanQuery<>();
        query.setFilter((key, user) -> Boolean.TRUE.equals(user.getActive()));
        return cache.query(query).getAll().stream()
                .map(Cache.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Find users by age range
     */
    public List<User> findByAgeRange(Integer minAge, Integer maxAge) {
        IgniteCache<Long, User> cache = getUserCache();
        ScanQuery<Long, User> query = new ScanQuery<>();
        query.setFilter((key, user) -> user.getAge() != null &&
                user.getAge() >= minAge && user.getAge() <= maxAge);
        return cache.query(query).getAll().stream()
                .map(Cache.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Count all users
     */
    public Long count() {
        return (long) getUserCache().size();
    }

    /**
     * Clear all users
     */
    public void deleteAll() {
        IgniteCache<Long, User> cache = getUserCache();
        cache.clear();
    }
}
