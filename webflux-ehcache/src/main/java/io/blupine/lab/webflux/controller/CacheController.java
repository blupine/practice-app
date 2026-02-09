package io.blupine.lab.webflux.controller;

import io.blupine.lab.webflux.service.CacheService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/mono/{id}")
    public Mono<String> getMono(@PathVariable String id) {
        return cacheService.getCachedMono(id);
    }

    @GetMapping("/flux/{id}")
    public Flux<String> getFlux(@PathVariable String id) {
        return cacheService.getCachedFlux(id);
    }

    @GetMapping("/no-cache/mono/{id}")
    public Mono<String> getUncachedMono(@PathVariable String id) {
        return cacheService.getUncachedMono(id);
    }

    @org.springframework.web.bind.annotation.PutMapping("/mono/{id}")
    public Mono<String> refreshMono(@PathVariable String id) {
        return cacheService.refreshMono(id);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("")
    public Mono<Void> clearCache() {
        return cacheService.clearCache();
    }
}
