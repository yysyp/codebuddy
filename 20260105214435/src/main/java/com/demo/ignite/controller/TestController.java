package com.demo.ignite.controller;

import com.demo.ignite.entity.Product;
import com.demo.ignite.entity.User;
import com.demo.ignite.service.ProductService;
import com.demo.ignite.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Controller
 * Provides endpoints to demonstrate various Ignite SQL query capabilities
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    /**
     * Get application status and cache statistics
     * GET /api/test/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("application", "Ignite Spring Boot Demo");
        status.put("status", "running");
        status.put("totalUsers", userService.countUsers());
        status.put("totalProducts", productService.countProducts());
        status.put("description", "Apache Ignite in-memory SQL cache demo");
        return ResponseEntity.ok(status);
    }

    /**
     * Test: Get all users
     * GET /api/test/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Test: Get all products
     * GET /api/test/products
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Test: Find active users
     * GET /api/test/users/active
     */
    @GetMapping("/users/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Test: Find users by age range
     * GET /api/test/users/age-range?minAge=30&maxAge=35
     */
    @GetMapping("/users/age-range")
    public ResponseEntity<List<User>> getUsersByAgeRange(
            @RequestParam Integer minAge,
            @RequestParam Integer maxAge) {
        List<User> users = userService.findByAgeRange(minAge, maxAge);
        Map<String, Object> response = new HashMap<>();
        response.put("minAge", minAge);
        response.put("maxAge", maxAge);
        response.put("count", users.size());
        response.put("users", users);
        return ResponseEntity.ok(users);
    }

    /**
     * Test: Find products by category
     * GET /api/test/products/category/{category}
     */
    @GetMapping("/products/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.findByCategory(category);
        return ResponseEntity.ok(products);
    }

    /**
     * Test: Find products by price range
     * GET /api/test/products/price-range?minPrice=0&maxPrice=100
     */
    @GetMapping("/products/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        List<Product> products = productService.findByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    /**
     * Test: Get available products
     * GET /api/test/products/available
     */
    @GetMapping("/products/available")
    public ResponseEntity<List<Product>> getAvailableProducts() {
        List<Product> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Test: Add a new user
     * POST /api/test/users
     */
    @PostMapping("/users")
    public ResponseEntity<User> addTestUser(@RequestBody User user) {
        if (user.getId() == null) {
            // Auto-generate ID if not provided
            Long newId = userService.countUsers() + 1;
            user.setId(newId);
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(System.currentTimeMillis());
        }
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * Test: Add a new product
     * POST /api/test/products
     */
    @PostMapping("/products")
    public ResponseEntity<Product> addTestProduct(@RequestBody Product product) {
        if (product.getId() == null) {
            // Auto-generate ID if not provided
            Long newId = productService.countProducts() + 1;
            product.setId(newId);
        }
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(System.currentTimeMillis());
        }
        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.ok(savedProduct);
    }

    /**
     * Test: Delete a user
     * DELETE /api/test/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteTestUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        response.put("userId", id.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Test: Delete a product
     * DELETE /api/test/products/{id}
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Map<String, String>> deleteTestProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        productService.deleteProduct(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        response.put("productId", id.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Test: Clear all data
     * DELETE /api/test/clear-all
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, String>> clearAllData() {
        userService.deleteAllUsers();
        productService.deleteAllProducts();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All data cleared from cache");
        return ResponseEntity.ok(response);
    }
}
