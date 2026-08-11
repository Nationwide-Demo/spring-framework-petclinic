package org.springframework.samples.petclinic.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ServiceUrlProperties.class)
public class WebClientConfig {

    @Bean
    WebClient customersWebClient(WebClient.Builder builder, ServiceUrlProperties urls) {
        return builder.baseUrl(urls.customers()).build();
    }

    @Bean
    WebClient visitsWebClient(WebClient.Builder builder, ServiceUrlProperties urls) {
        return builder.baseUrl(urls.visits()).build();
    }

}
