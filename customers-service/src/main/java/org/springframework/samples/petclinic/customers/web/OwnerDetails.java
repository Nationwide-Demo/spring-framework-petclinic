package org.springframework.samples.petclinic.customers.web;

import java.util.List;

import org.springframework.samples.petclinic.customers.model.Owner;

public record OwnerDetails(
    Integer id,
    String firstName,
    String lastName,
    String address,
    String city,
    String telephone,
    List<PetDetails> pets) {

    static OwnerDetails of(Owner owner) {
        return new OwnerDetails(
            owner.getId(),
            owner.getFirstName(),
            owner.getLastName(),
            owner.getAddress(),
            owner.getCity(),
            owner.getTelephone(),
            owner.getPets().stream().map(PetDetails::of).toList());
    }

}
