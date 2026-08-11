package org.springframework.samples.petclinic.vets.web;

import java.util.List;

import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.SpecialtyRepository;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class VetResource {

    private final VetRepository vets;

    private final SpecialtyRepository specialties;

    VetResource(VetRepository vets, SpecialtyRepository specialties) {
        this.vets = vets;
        this.specialties = specialties;
    }

    @GetMapping("/vets")
    public List<VetDetails> findAll() {
        return vets.findAllByOrderByLastNameAscFirstNameAsc().stream().map(VetDetails::of).toList();
    }

    @GetMapping("/vets/{vetId}")
    public VetDetails findVet(@PathVariable int vetId) {
        return vets.findById(vetId)
            .map(VetDetails::of)
            .orElseThrow(() -> new ResourceNotFoundException("Vet " + vetId + " not found"));
    }

    @GetMapping("/specialties")
    public List<String> findSpecialties() {
        return specialties.findAllByOrderByNameAsc().stream().map(Specialty::getName).toList();
    }

}
