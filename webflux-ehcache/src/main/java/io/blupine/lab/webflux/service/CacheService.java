package io.blupine.lab.webflux.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.blupine.lab.webflux.annotation.ReactorCacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    @ReactorCacheable(name = "monos", key = "#id")
    public Mono<String> getCachedMono(String id) {
        log.info("getCachedMono 실행 - id: {}", id);
        return Mono.just("Cached Mono Result for " + id)
                .delayElement(Duration.ofMillis(500));
    }

    @ReactorCacheable(name = "fluxes", key = "#id")
    public Flux<String> getCachedFlux(String id) {
        log.info("getCachedFlux 실행 - id: {}", id);
        return Flux.just("A", "B", "C", "for " + id)
                .delayElements(Duration.ofMillis(100));
        // .cache(); // 제거됨
    }

    public Mono<String> getUncachedMono(String id) {
        log.info("getUncachedMono 실행 - id: {}", id);
        return Mono.just("Uncached Mono Result for " + id)
                .delayElement(Duration.ofMillis(500));
    }

    // @CachePut replacement logic not implemented in Aspect yet for this demo
    // @CachePut(value = "monos", key = "#id")
    public Mono<String> refreshMono(String id) {
        log.info("refreshMono 실행 - id: {}", id);
        return Mono.just("Refreshed Mono Result for " + id);
    }

    // @CacheEvict replacement logic not implemented in Aspect yet for this demo
    // @CacheEvict(value = {"monos", "fluxes"}, allEntries = true)
    public Mono<Void> clearCache() {
        log.info("All Caches Cleared");
        return Mono.empty();
    }
}
