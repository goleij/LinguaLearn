package com.germanlearning.service.external;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import java.time.Duration;

/**
 * One HTTP client for all outbound calls, with timeouts that keep a slow third
 * party from holding a request thread.
 */
@Configuration
public class ExternalApiConfig {

    @Bean
    public RestClient externalRestClient(ExternalApiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", properties.getUserAgent())
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
