package org.springframework.samples.petclinic.vets.web;

import java.util.List;

import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;

public record VetDetails(Integer id, String firstName, String lastName, List<String> specialties) {

    static VetDetails of(Vet vet) {
        return new VetDetails(
            vet.getId(),
            vet.getFirstName(),
            vet.getLastName(),
            vet.getSpecialties().stream().map(Specialty::getName).toList());
    }

}
