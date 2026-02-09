package io.blupine.lab.webflux.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Ehcache 3.x using JCache (JSR-107)
        javax.cache.CacheManager checkCacheManager = Caching.getCachingProvider().getCacheManager();

        // Define configuration programmatically
        javax.cache.configuration.MutableConfiguration<Object, Object> configuration = new javax.cache.configuration.MutableConfiguration<>()
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));

        checkCacheManager.createCache("monos", configuration);
        checkCacheManager.createCache("fluxes", configuration);

        return new JCacheCacheManager(checkCacheManager);
    }
}
