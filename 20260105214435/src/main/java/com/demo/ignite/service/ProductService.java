package com.demo.ignite.service;

import com.demo.ignite.entity.Product;
import com.demo.ignite.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Product Service
 * Business logic layer for Product operations
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Create or update a product
     */
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * Get product by ID
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Delete product by ID
     */
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * Find products by category
     */
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * Get available products
     */
    public List<Product> getAvailableProducts() {
        return productRepository.findAvailableProducts();
    }

    /**
     * Find products by price range
     */
    public List<Product> findByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    /**
     * Count total products
     */
    public Long countProducts() {
        return productRepository.count();
    }

    /**
     * Delete all products
     */
    public void deleteAllProducts() {
        productRepository.deleteAll();
    }
}
