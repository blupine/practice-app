package io.blupine.lab.webflux.aspect;

import io.blupine.lab.webflux.annotation.ReactorCacheable;
import io.blupine.lab.webflux.config.ReactorCacheManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.ehcache.Cache;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import io.micrometer.core.instrument.MeterRegistry;

@Aspect
@Component
public class ReactorCacheAspect {

    private final ReactorCacheManager cacheManager;
    private final MeterRegistry meterRegistry;

    public ReactorCacheAspect(ReactorCacheManager cacheManager, MeterRegistry meterRegistry) {
        this.cacheManager = cacheManager;
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(reactorCacheable)")
    public Object cache(ProceedingJoinPoint joinPoint, ReactorCacheable reactorCacheable) throws Throwable {
        String cacheName = reactorCacheable.name();
        Object[] args = joinPoint.getArgs();

        // Simple key generation strategy: use the first argument as String
        // In a real scenario, a more robust key generator would be needed
        if (args.length == 0) {
            return joinPoint.proceed();
        }
        String key = String.valueOf(args[0]);

        Cache<String, Object> cache = cacheManager.getCache(cacheName, String.class, Object.class);
        if (cache != null) {
            Object cachedValue = cache.get(key);
            if (cachedValue != null) {
                meterRegistry.counter("cache.gets", "cache", cacheName, "result", "hit").increment();
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                Class<?> returnType = signature.getReturnType();

                if (Mono.class.isAssignableFrom(returnType)) {
                    return Mono.just(cachedValue);
                } else if (Flux.class.isAssignableFrom(returnType) && cachedValue instanceof List) {
                    return Flux.fromIterable((List<?>) cachedValue);
                }
            } else {
                meterRegistry.counter("cache.gets", "cache", cacheName, "result", "miss").increment();
            }
        }

        Object result = joinPoint.proceed();

        if (result instanceof Mono) {
            return ((Mono<?>) result).doOnNext(value -> {
                if (cache != null) {
                    cache.put(key, value);
                    meterRegistry.counter("cache.puts", "cache", cacheName).increment();
                }
            });
        } else if (result instanceof Flux) {
            return ((Flux<?>) result).collectList()
                    .doOnNext(list -> {
                        if (cache != null) {
                            cache.put(key, list);
                            meterRegistry.counter("cache.puts", "cache", cacheName).increment();
                        }
                    })
                    .flatMapMany(Flux::fromIterable);
        }

        return result;
    }
}
