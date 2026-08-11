package org.springframework.samples.petclinic.gateway.aggregate;

import java.util.List;

public record OwnerDetails(Integer id, String firstName, String lastName, String address, String city,
                           String telephone, List<PetDetails> pets) {

    OwnerDetails withPets(List<PetDetails> pets) {
        return new OwnerDetails(id, firstName, lastName, address, city, telephone, pets);
    }

}
