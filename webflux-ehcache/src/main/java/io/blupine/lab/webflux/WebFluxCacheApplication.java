package io.blupine.lab.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WebFluxCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebFluxCacheApplication.class, args);
    }

}
