package com.apiarchlab.restclient.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient Configuration for non-blocking HTTP calls.
 * 
 * WebClient is the modern replacement for RestTemplate.
 * Key differences from RestTemplate:
 * - Non-blocking: Uses Netty under the hood
 * - Reactive: Returns Mono/Flux instead of blocking calls
 * - Better for high-concurrency scenarios
 * - Works seamlessly with Spring MVC (syncing back to CompletableFuture)
 */
@Configuration
@Slf4j
public class WebClientConfig {

    /**
     * Main WebClient bean with timeout and connection pooling.
     */
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = createHttpClient();
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequestFilter())
                .filter(logResponseFilter())
                .build();
    }

    /**
     * Create HttpClient with timeout and connection pool settings.
     */
    private HttpClient createHttpClient() {
        return HttpClient.create()
                .secure()  // Support HTTPS
                .responseTimeout(Duration.ofSeconds(5))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS))
                );
    }

    /**
     * Filter to log requests.
     */
    private ExchangeFilterFunction logRequestFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug(">>> {} {} - Content-Type: {}",
                    clientRequest.getMethod(),
                    clientRequest.getURL(),
                    clientRequest.getHeaders().getContentType());
            return Mono.just(clientRequest);
        });
    }

    /**
     * Filter to log responses.
     */
    private ExchangeFilterFunction logResponseFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("<<< {} {} - Status: {}",
                    clientResponse.getRequest().getMethod(),
                    clientResponse.getURL(),
                    clientResponse.getStatusCode());
            return Mono.just(clientResponse);
        });
    }
}

