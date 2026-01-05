package com.demo.ignite;

import com.demo.ignite.entity.Product;
import com.demo.ignite.entity.User;
import com.demo.ignite.service.ProductService;
import com.demo.ignite.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data Initializer
 * Populates the cache with sample data when the application starts
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final ProductService productService;

    public DataInitializer(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========================================");
        System.out.println("Initializing sample data in Ignite cache...");
        System.out.println("========================================");

        // Initialize sample users
        initializeUsers();

        // Initialize sample products
        initializeProducts();

        System.out.println("========================================");
        System.out.println("Sample data initialization completed!");
        System.out.println("========================================");
    }

    private void initializeUsers() {
        Long currentTime = System.currentTimeMillis();

        User user1 = new User(1L, "john_doe", "john@example.com", "John Doe",
                "Software developer with 5 years experience", 28, true, currentTime);

        User user2 = new User(2L, "jane_smith", "jane@example.com", "Jane Smith",
                "Full-stack developer specializing in Java and JavaScript", 32, true, currentTime);

        User user3 = new User(3L, "bob_wilson", "bob@example.com", "Bob Wilson",
                "DevOps engineer and cloud architect", 35, true, currentTime);

        User user4 = new User(4L, "alice_brown", "alice@example.com", "Alice Brown",
                "Data scientist and ML engineer", 29, false, currentTime);

        User user5 = new User(5L, "charlie_davis", "charlie@example.com", "Charlie Davis",
                "Backend developer with expertise in Spring Boot", 31, true, currentTime);

        userService.saveUser(user1);
        userService.saveUser(user2);
        userService.saveUser(user3);
        userService.saveUser(user4);
        userService.saveUser(user5);

        System.out.println("✓ 5 sample users created in UserCache");
    }

    private void initializeProducts() {
        Long currentTime = System.currentTimeMillis();

        Product product1 = new Product(1L, "Laptop", "High-performance laptop for developers",
                1299.99, "Electronics", 50, true, currentTime);

        Product product2 = new Product(2L, "Wireless Mouse", "Ergonomic wireless mouse",
                29.99, "Accessories", 200, true, currentTime);

        Product product3 = new Product(3L, "Mechanical Keyboard", "RGB mechanical keyboard",
                149.99, "Accessories", 100, true, currentTime);

        Product product4 = new Product(4L, "Monitor Stand", "Adjustable monitor stand",
                79.99, "Accessories", 75, true, currentTime);

        Product product5 = new Product(5L, "USB-C Hub", "Multi-port USB-C hub",
                49.99, "Accessories", 150, true, currentTime);

        Product product6 = new Product(6L, "Webcam", "HD 1080p webcam",
                89.99, "Electronics", 80, true, currentTime);

        Product product7 = new Product(7L, "Desk Lamp", "LED desk lamp with adjustable brightness",
                39.99, "Office Supplies", 120, true, currentTime);

        Product product8 = new Product(8L, "Notebook Set", "Premium notebook set",
                19.99, "Office Supplies", 300, true, currentTime);

        productService.saveProduct(product1);
        productService.saveProduct(product2);
        productService.saveProduct(product3);
        productService.saveProduct(product4);
        productService.saveProduct(product5);
        productService.saveProduct(product6);
        productService.saveProduct(product7);
        productService.saveProduct(product8);

        System.out.println("✓ 8 sample products created in ProductCache");
    }
}
