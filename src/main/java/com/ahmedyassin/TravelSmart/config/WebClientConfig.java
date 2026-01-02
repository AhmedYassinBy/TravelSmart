package com.ahmedyassin.TravelSmart.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${api.timeout.connect:10}")
    private int connectTimeout;

    @Value("${api.timeout.read:30}")
    private int readTimeout;

    @Bean
    public WebClient.Builder webClientBuilder() {
        // Configuration du HttpClient avec timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout * 1000)
                .responseTimeout(Duration.ofSeconds(readTimeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.SECONDS)));

        // Augmenter la taille maximale du buffer pour les grandes réponses
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)) // 16MB
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies);
    }

    @Bean("amadeusWebClient")
    public WebClient amadeusWebClient(WebClient.Builder builder, @Value("${amadeus.api.base-url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("aviationStackWebClient")
    public WebClient aviationStackWebClient(WebClient.Builder builder, @Value("${aviationstack.api.base-url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("rapidApiWebClient")
    public WebClient rapidApiWebClient(WebClient.Builder builder, @Value("${rapidapi.booking.base-url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}

