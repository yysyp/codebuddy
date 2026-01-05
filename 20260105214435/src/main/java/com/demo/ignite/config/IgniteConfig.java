package com.demo.ignite.config;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.marshaller.jdk.JdkMarshaller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Apache Ignite Configuration Class
 * Configures Apache Ignite in embedded mode for in-memory SQL caching
 */
@Configuration
public class IgniteConfig {

    /**
     * Configure Ignite Bean
     * This bean will start the Ignite node in embedded mode
     *
     * @return Ignite instance
     */
    @Bean(destroyMethod = "close")
    public Ignite igniteInstance() {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setIgniteInstanceName("IgniteSpringBootDemo");
        cfg.setPeerClassLoadingEnabled(true);

        // Use JDK marshaller to avoid binary serialization issues
        cfg.setMarshaller(new JdkMarshaller());

        // Configure caches programmatically
        cfg.setCacheConfiguration(
            configureUserCache(),
            configureProductCache()
        );

        return Ignition.start(cfg);
    }

    /**
     * Configure User Cache
     */
    private CacheConfiguration<Long, Object> configureUserCache() {
        CacheConfiguration<Long, Object> cacheCfg = new CacheConfiguration<>("UserCache");
        cacheCfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        cacheCfg.setBackups(1);
        return cacheCfg;
    }

    /**
     * Configure Product Cache
     */
    private CacheConfiguration<Long, Object> configureProductCache() {
        CacheConfiguration<Long, Object> cacheCfg = new CacheConfiguration<>("ProductCache");
        cacheCfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        cacheCfg.setBackups(1);
        return cacheCfg;
    }
}
