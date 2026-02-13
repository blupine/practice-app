package io.blupine.lab.webflux.config;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class ReactorCacheManager {

    private CacheManager cacheManager;

    @PostConstruct
    public void init() {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("monos",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Object.class,
                                ResourcePoolsBuilder.heap(100)))
                .withCache("fluxes",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Object.class,
                                ResourcePoolsBuilder.heap(100)))
                .build(true);
    }

    public <K, V> Cache<K, V> getCache(String name, Class<K> keyType, Class<V> valueType) {
        return cacheManager.getCache(name, keyType, valueType);
    }

    @PreDestroy
    public void close() {
        if (cacheManager != null) {
            cacheManager.close();
        }
    }
}
