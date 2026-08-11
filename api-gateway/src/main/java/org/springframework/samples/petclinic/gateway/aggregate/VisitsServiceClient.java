package org.springframework.samples.petclinic.gateway.aggregate;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class VisitsServiceClient {

    private static final ParameterizedTypeReference<Map<Integer, List<VisitDetails>>> VISITS_BY_PET =
        new ParameterizedTypeReference<>() {
        };

    private final WebClient webClient;

    private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public VisitsServiceClient(@Qualifier("visitsWebClient") WebClient webClient,
                               ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.webClient = webClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    /**
     * Visits are supporting detail on the owner page: when visits-service is unavailable the
     * owner and its pets are still rendered, with no visits, rather than failing the whole page.
     */
    public Mono<Map<Integer, List<VisitDetails>>> findVisitsByPetIds(List<Integer> petIds) {
        if (petIds.isEmpty()) {
            return Mono.just(Map.of());
        }
        Mono<Map<Integer, List<VisitDetails>>> visits = webClient.get()
            .uri(uriBuilder -> uriBuilder.path("/pets/visits").queryParam("petId", petIds).build())
            .retrieve()
            .bodyToMono(VISITS_BY_PET);
        return circuitBreakerFactory.create("visits-service").run(visits, throwable -> Mono.just(Map.of()));
    }

}
