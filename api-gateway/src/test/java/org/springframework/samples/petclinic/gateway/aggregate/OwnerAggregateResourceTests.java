package org.springframework.samples.petclinic.gateway.aggregate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.main.allow-bean-definition-overriding=true")
@Import(OwnerAggregateResourceTests.StubUpstreams.class)
class OwnerAggregateResourceTests {

    private static final String OWNER_JSON = """
        {"id":6,"firstName":"Jean","lastName":"Coleman","address":"105 N. Lake St.","city":"Monona",
         "telephone":"6085552654",
         "pets":[{"id":7,"name":"Samantha","birthDate":"2012-09-04","typeId":1,"type":"cat"},
                 {"id":8,"name":"Max","birthDate":"2012-09-04","typeId":1,"type":"cat"}]}""";

    private static final String VISITS_JSON = """
        {"7":[{"id":1,"petId":7,"date":"2013-01-01","description":"rabies shot"}],
         "8":[{"id":2,"petId":8,"date":"2013-01-02","description":"neutered"}]}""";

    @Autowired
    private WebTestClient client;

    @Autowired
    private StubUpstreams stubs;

    @Test
    void shouldComposeOwnerWithVisitsOfEachPet() {
        stubs.responses.put("/pets/visits", request -> json(VISITS_JSON));

        client.get().uri("/api/gateway/owners/6").exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.lastName").isEqualTo("Coleman")
            .jsonPath("$.pets[0].name").isEqualTo("Samantha")
            .jsonPath("$.pets[0].visits[0].description").isEqualTo("rabies shot")
            .jsonPath("$.pets[1].visits[0].description").isEqualTo("neutered");

        assertThat(stubs.requestedUris).anyMatch(uri -> uri.contains("petId=7") && uri.contains("petId=8"));
    }

    @Test
    void shouldStillReturnOwnerWhenVisitsServiceFails() {
        stubs.responses.put("/pets/visits",
            request -> ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());

        client.get().uri("/api/gateway/owners/6").exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.lastName").isEqualTo("Coleman")
            .jsonPath("$.pets[0].visits").isEmpty();
    }

    private static ClientResponse json(String body) {
        return ClientResponse.create(HttpStatus.OK)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(body)
            .build();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class StubUpstreams {

        final Map<String, Function<ClientRequest, ClientResponse>> responses = new HashMap<>();

        final java.util.List<String> requestedUris = new java.util.ArrayList<>();

        StubUpstreams() {
            responses.put("/owners/6", request -> json(OWNER_JSON));
        }

        @Bean
        WebClient customersWebClient() {
            return stubClient();
        }

        @Bean
        WebClient visitsWebClient() {
            return stubClient();
        }

        private WebClient stubClient() {
            return WebClient.builder()
                .baseUrl("http://stub")
                .exchangeFunction(request -> {
                    requestedUris.add(request.url().toString());
                    return Mono.just(responses.entrySet().stream()
                        .filter(entry -> request.url().getPath().equals(entry.getKey()))
                        .findFirst()
                        .map(entry -> entry.getValue().apply(request))
                        .orElseGet(() -> ClientResponse.create(HttpStatusCode.valueOf(404)).build()));
                })
                .build();
        }

    }

}
