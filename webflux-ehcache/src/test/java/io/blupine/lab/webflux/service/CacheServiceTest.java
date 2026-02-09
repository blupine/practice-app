package io.blupine.lab.webflux.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CacheServiceTest {

    @Autowired
    private CacheService cacheService;

    @Test
    public void testMonoCaching() {
        String id = "test-1";

        // First call - should take some time (simulated delay)
        long start = System.currentTimeMillis();
        StepVerifier.create(cacheService.getCachedMono(id))
                .expectNextMatches(s -> s.contains(id))
                .verifyComplete();
        long duration1 = System.currentTimeMillis() - start;

        // Second call - should be instant
        start = System.currentTimeMillis();
        StepVerifier.create(cacheService.getCachedMono(id))
                .expectNextMatches(s -> s.contains(id))
                .verifyComplete();
        long duration2 = System.currentTimeMillis() - start;

        // Assert that the second call was much faster
        // The first call has a 500ms delay.
        assertThat(duration1).isGreaterThan(500);
        assertThat(duration2).isLessThan(100);
    }
}
