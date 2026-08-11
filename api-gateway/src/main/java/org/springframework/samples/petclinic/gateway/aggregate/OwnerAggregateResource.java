package org.springframework.samples.petclinic.gateway.aggregate;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * The owner page needs data from two services. Composing it here keeps the SPA on a single
 * request and replaces the join the monolith did in SQL.
 */
@RestController
@RequestMapping("/api/gateway")
class OwnerAggregateResource {

    private final CustomersServiceClient customers;

    private final VisitsServiceClient visits;

    OwnerAggregateResource(CustomersServiceClient customers, VisitsServiceClient visits) {
        this.customers = customers;
        this.visits = visits;
    }

    @GetMapping("/owners/{ownerId}")
    public Mono<OwnerDetails> findOwner(@PathVariable int ownerId) {
        return customers.findOwner(ownerId).flatMap(this::withVisits);
    }

    private Mono<OwnerDetails> withVisits(OwnerDetails owner) {
        List<Integer> petIds = owner.pets().stream().map(PetDetails::id).toList();
        return visits.findVisitsByPetIds(petIds).map(visitsByPetId -> merge(owner, visitsByPetId));
    }

    private OwnerDetails merge(OwnerDetails owner, Map<Integer, List<VisitDetails>> visitsByPetId) {
        List<PetDetails> pets = owner.pets().stream()
            .map(pet -> pet.withVisits(visitsByPetId.getOrDefault(pet.id(), List.of())))
            .toList();
        return owner.withPets(pets);
    }

}
