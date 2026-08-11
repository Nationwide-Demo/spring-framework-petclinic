package org.springframework.samples.petclinic.customers.web;

import java.time.LocalDate;

import org.springframework.samples.petclinic.customers.model.Pet;

public record PetDetails(
    Integer id,
    String name,
    LocalDate birthDate,
    Integer typeId,
    String type,
    Integer ownerId,
    String ownerName) {

    static PetDetails of(Pet pet) {
        return new PetDetails(
            pet.getId(),
            pet.getName(),
            pet.getBirthDate(),
            pet.getType() == null ? null : pet.getType().getId(),
            pet.getType() == null ? null : pet.getType().getName(),
            pet.getOwner() == null ? null : pet.getOwner().getId(),
            pet.getOwner() == null ? null
                : pet.getOwner().getFirstName() + " " + pet.getOwner().getLastName());
    }

}
