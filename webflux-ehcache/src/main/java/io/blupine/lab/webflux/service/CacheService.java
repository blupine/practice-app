package io.blupine.lab.webflux.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    // 참고: Ehcache(JCache)는 동기식 캐시이므로, Spring Cache 추상화가
    // Mono/Flux 리턴 타입을 지원하더라도 내부적으로 캐시 조회/저장이 동기적으로 수행됨(Blocking).
    // 캐시 히트 시 저장된 값을 기반으로 Mono가 리턴되지만, 이 과정에서 Event Loop가 차단될 수 있음.
    @Cacheable("monos")
    public Mono<String> getCachedMono(String id) {
        log.info("getCachedMono 실행 - id: {}", id);
        return Mono.just("Cached Mono Result for " + id)
                .delayElement(Duration.ofMillis(500));
    }

    // Flux의 경우, Spring이 내부적으로 List 등으로 수집(collect)하여 캐싱하고
    // 캐시 히트 시 다시 Flux로 변환하여 리턴하는 방식으로 동작함 (동기적 수행)
    @Cacheable("fluxes")
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

    @CachePut(value = "monos", key = "#id")
    public Mono<String> refreshMono(String id) {
        log.info("refreshMono 실행 - id: {}", id);
        return Mono.just("Refreshed Mono Result for " + id);
    }

    @CacheEvict(value = {"monos", "fluxes"}, allEntries = true)
    public Mono<Void> clearCache() {
        log.info("All Caches Cleared");
        return Mono.empty();
    }
}
