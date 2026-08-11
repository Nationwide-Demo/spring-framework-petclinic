package org.springframework.samples.petclinic.visits.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class VisitResource {

    private final VisitRepository visits;

    VisitResource(VisitRepository visits) {
        this.visits = visits;
    }

    @GetMapping("/pets/{petId}/visits")
    public List<Visit> findByPetId(@PathVariable int petId) {
        return visits.findByPetIdOrderByDateDesc(petId);
    }

    /**
     * Batch lookup so a caller rendering an owner with several pets needs a single request.
     */
    @GetMapping("/pets/visits")
    public Map<Integer, List<Visit>> findByPetIds(@RequestParam("petId") List<Integer> petIds) {
        return visits.findByPetIdInOrderByDateDesc(petIds).stream()
            .collect(Collectors.groupingBy(Visit::getPetId));
    }

    @PostMapping("/pets/{petId}/visits")
    @ResponseStatus(HttpStatus.CREATED)
    public Visit createVisit(@PathVariable int petId, @Valid @RequestBody VisitRequest request) {
        Visit visit = new Visit();
        visit.setPetId(petId);
        visit.setDate(request.date() == null ? LocalDate.now() : request.date());
        visit.setDescription(request.description());
        return visits.save(visit);
    }

}
