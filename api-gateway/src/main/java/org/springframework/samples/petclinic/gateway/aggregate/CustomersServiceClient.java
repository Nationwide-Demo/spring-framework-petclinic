package org.springframework.samples.petclinic.gateway.aggregate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CustomersServiceClient {

    private final WebClient webClient;

    public CustomersServiceClient(@Qualifier("customersWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<OwnerDetails> findOwner(int ownerId) {
        return webClient.get().uri("/owners/{ownerId}", ownerId).retrieve().bodyToMono(OwnerDetails.class);
    }

}
