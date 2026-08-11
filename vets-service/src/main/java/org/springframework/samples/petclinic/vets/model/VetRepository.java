package org.springframework.samples.petclinic.vets.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VetRepository extends JpaRepository<Vet, Integer> {

    List<Vet> findAllByOrderByLastNameAscFirstNameAsc();

}
