package io.blupine.lab.webflux.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    // 참고: Spring Framework 6 (Boot 3) 이상에서 Caffeine의 asyncCacheMode를 활성화하면
    // 리턴 타입이 Mono/Flux일 때 자동으로 CompletableFuture 등으로 변환되어 결과 값이 캐싱됨
    // 따라서 Mono 자체에 .cache()를 붙이지 않아도, 캐시 히트 시 저장된 값을 기반으로 재생성된 Mono가 리턴되므로
    // 실제 로직(로그 출력 등)은 재실행되지 않음
    @Cacheable("monos")
    public Mono<String> getCachedMono(String id) {
        log.info("getCachedMono 실행 - id: {}", id);
        return Mono.just("Cached Mono Result for " + id)
                .delayElement(Duration.ofMillis(500));
        // .cache(); // 제거됨: Spring Cache 추상화가 값을 캐싱해주므로 불필요
    }

    // Flux의 경우, Spring이 내부적으로 List 등으로 수집(collect)하여 캐싱하고
    // 캐시 히트 시 다시 Flux로 변환하여 리턴하는 방식으로 동작함
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
}
