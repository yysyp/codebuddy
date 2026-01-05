package com.demo.ignite.repository;

import com.demo.ignite.entity.Product;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Repository for Ignite Cache Operations
 * Provides CRUD operations and queries on Product entities
 */
@Repository
public class ProductRepository {

    private static final String CACHE_NAME = "ProductCache";

    @Autowired
    private Ignite ignite;

    /**
     * Get or create Product Cache
     */
    private IgniteCache<Long, Product> getProductCache() {
        return ignite.getOrCreateCache(CACHE_NAME);
    }

    /**
     * Save or update a product
     */
    public Product save(Product product) {
        IgniteCache<Long, Product> cache = getProductCache();
        cache.put(product.getId(), product);
        return product;
    }

    /**
     * Find product by ID
     */
    public Product findById(Long id) {
        IgniteCache<Long, Product> cache = getProductCache();
        return cache.get(id);
    }

    /**
     * Find all products
     */
    public List<Product> findAll() {
        IgniteCache<Long, Product> cache = getProductCache();
        List<Product> products = new ArrayList<>();
        cache.forEach(entry -> products.add(entry.getValue()));
        return products;
    }

    /**
     * Delete product by ID
     */
    public void deleteById(Long id) {
        IgniteCache<Long, Product> cache = getProductCache();
        cache.remove(id);
    }

    /**
     * Find products by category
     */
    public List<Product> findByCategory(String category) {
        IgniteCache<Long, Product> cache = getProductCache();
        ScanQuery<Long, Product> query = new ScanQuery<>();
        query.setFilter((key, product) -> category.equals(product.getCategory()));
        return cache.query(query).getAll().stream()
                .map(Cache.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Find available products
     */
    public List<Product> findAvailableProducts() {
        IgniteCache<Long, Product> cache = getProductCache();
        ScanQuery<Long, Product> query = new ScanQuery<>();
        query.setFilter((key, product) -> Boolean.TRUE.equals(product.getAvailable()));
        return cache.query(query).getAll().stream()
                .map(Cache.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Find products by price range
     */
    public List<Product> findByPriceRange(Double minPrice, Double maxPrice) {
        IgniteCache<Long, Product> cache = getProductCache();
        ScanQuery<Long, Product> query = new ScanQuery<>();
        query.setFilter((key, product) -> product.getPrice() != null &&
                product.getPrice() >= minPrice && product.getPrice() <= maxPrice);
        return cache.query(query).getAll().stream()
                .map(Cache.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Count all products
     */
    public Long count() {
        return (long) getProductCache().size();
    }

    /**
     * Clear all products
     */
    public void deleteAll() {
        IgniteCache<Long, Product> cache = getProductCache();
        cache.clear();
    }
}
