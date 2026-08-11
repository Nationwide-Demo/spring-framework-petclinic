package org.springframework.samples.petclinic.customers.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Integer> {

}
